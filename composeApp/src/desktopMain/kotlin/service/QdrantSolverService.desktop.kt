package service

import api.aidevs.model.unknow.people.PersonWithMetadata
import api.aidevs.model.unknow.search.UnknowNewsWithMetadata

actual interface QdrantSolverService {
    actual suspend fun createCollection(collectionName: String): Boolean
    actual suspend fun upsert(collectionName: String, unknowNews: List<UnknowNewsWithMetadata>)
    actual suspend fun search(collectionName: String, embedding: List<Double>): Long
    actual suspend fun upsert(collectionName: String, personWithMetadata: List<PersonWithMetadata>, shitJustForGiggles: Boolean)
}