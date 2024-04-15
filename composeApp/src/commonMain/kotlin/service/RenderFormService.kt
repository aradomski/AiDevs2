package service

import api.renderform.RenderfromApi
import api.renderform.model.RenderRequest

class RenderFormService(private val renderfromApi: RenderfromApi) {

    suspend fun render(renderRequest: RenderRequest) =
        renderfromApi.render(renderRequest)
}