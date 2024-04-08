package service

import api.model.unknow.people.PersonWithMetadata
import api.model.unknow.search.UnknowNewsWithMetadata

expect interface QdrantSolverService {

    suspend fun createCollection(collectionName: String): Boolean
    suspend fun upsert(collectionName: String, unknowNews: List<UnknowNewsWithMetadata>)
    suspend fun search(collectionName: String, embedding: List<Double>): Long
    suspend fun upsert(
        collectionName: String,
        personWithMetadata: List<PersonWithMetadata>,
        shitJustForGiggles: Boolean
    )
}