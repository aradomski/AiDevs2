package service

import api.aidevs.model.unknow.people.PersonWithMetadata
import api.aidevs.model.unknow.search.UnknowNewsWithMetadata
import io.qdrant.client.PointIdFactory.id
import io.qdrant.client.QdrantClient
import io.qdrant.client.ValueFactory.value
import io.qdrant.client.VectorsFactory.vectors
import io.qdrant.client.grpc.Collections
import io.qdrant.client.grpc.Points.PointStruct
import io.qdrant.client.grpc.Points.SearchPoints
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class QdrantSolverServiceImpl(private val client: QdrantClient) : QdrantSolverService {
    override suspend fun createCollection(collectionName: String): Boolean {
        var existedBefore: Boolean
        withContext(Dispatchers.IO) {
            val collectionList = client.listCollectionsAsync().get()

            if (!collectionList.contains(collectionName)) {
                existedBefore = false
                client.createCollectionAsync(
                    collectionName,
                    Collections.VectorParams.newBuilder().setDistance(Collections.Distance.Cosine)
                        .setSize(1536).setOnDisk(true).build()
                ).get()
            } else {
                existedBefore = true
            }
        }
        return existedBefore
    }

    override suspend fun upsert(collectionName: String, unknowNews: List<UnknowNewsWithMetadata>) {
        withContext(Dispatchers.IO) {
            val operationInfo = client.upsertAsync(collectionName, unknowNews.map {
                val mapOf = mapOf(
                    "date" to value(it.date),
                    "info" to value(it.info),
                    "title" to value(it.title),
                    "url" to value(it.url),
                )

                PointStruct.newBuilder().setId(id(it.uuid.toLong()))
                    .setVectors(vectors(it.vector.map { it.toFloat() })).putAllPayload(mapOf)
                    .build()
            }).get()
        }
    }

    override suspend fun upsert(
        collectionName: String,
        personWithMetadata: List<PersonWithMetadata>,
        shitJustForGiggles: Boolean
    ) {
        withContext(Dispatchers.IO) {
            val operationInfo = client.upsertAsync(collectionName, personWithMetadata.map {
                val mapOf = mapOf(
                    "imie" to value(it.imie),
                    "nazwisko" to value(it.nazwisko),
                    "o_mnie" to value(it.oMnie),
                    "ulubiona_postac_z_kapitana_bomby" to value(it.ulubionaPostacZKapitanaBomby),
                    "ulubiony_film" to value(it.ulubionyFilm),
                    "ulubiony_kolor" to value(it.ulubionyKolor),
                    "ulubiony_serial" to value(it.ulubionySerial),
                    "wiek" to value(it.wiek.toLong()),
                )
                PointStruct.newBuilder().setId(id(it.uuid.toLong()))
                    .setVectors(vectors(it.vector.map { it.toFloat() })).putAllPayload(mapOf)
                    .build()
            }).get()
        }

    }

    override suspend fun search(collectionName: String, embedding: List<Double>): Long {
        val scoredPoints = withContext(Dispatchers.IO) {
            client
                .searchAsync(
                    SearchPoints.newBuilder()
                        .setCollectionName(collectionName)
                        .setLimit(1)
                        .addAllVector(embedding.map { it.toFloat() })
                        .build()
                )
                .get()
        }
        println("-----------")
        println(scoredPoints.get(0).id.num)
        println("-----------")
        return scoredPoints.get(0).id.num
    }
}