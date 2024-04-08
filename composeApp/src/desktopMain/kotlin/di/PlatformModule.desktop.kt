package di

import io.qdrant.client.QdrantClient
import io.qdrant.client.QdrantGrpcClient
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import service.QdrantSolverService
import service.QdrantSolverServiceImpl


actual fun platformModule() = module {
    single {
        QdrantClient(QdrantGrpcClient.newBuilder("localhost", 6334, false).build())
    }
    singleOf(::QdrantSolverServiceImpl) bind QdrantSolverService::class
}