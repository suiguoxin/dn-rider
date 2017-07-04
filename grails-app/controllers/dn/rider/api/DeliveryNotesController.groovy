package dn.rider.api

import grails.converters.JSON
import grails.plugins.rest.client.RestBuilder
import io.swagger.annotations.Api
import io.swagger.annotations.ApiImplicitParam
import io.swagger.annotations.ApiImplicitParams
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.ApiResponses

@Api(value = "/api", tags = ["DeliveryNotes"], description = "Dn-Rider APIs")
class DeliveryNotesController {

    def nexusConsumerService
    def jsonSchemaValidationService

    @ApiOperation(
            value = "Récupèrer la liste des applications avec note de livraison",
            nickname = "applications",
            produces = "application/json",
            consumes = "application/json",
            httpMethod = "GET"
    )
    @ApiImplicitParams([
            @ApiImplicitParam(name = "format",
                    paramType = "query",
                    required = false,
                    value = "text/json",
                    dataType = "string")
    ])
    def showApps() {
        String format = params.format ?: 'json'

        def apps = nexusConsumerService.getApps()

        if (format.toUpperCase() == 'TEXT')
            render apps
        else render apps as JSON
    }

    @ApiOperation(
            value = "Récupèrer la liste des note de livraison",
            nickname = "deliveryNotes/{app}/{releaseType}?",
            produces = "application/json",
            consumes = "application/json",
            httpMethod = "GET"
    )
    @ApiImplicitParams([
            @ApiImplicitParam(name = "app",
                    paramType = "path",
                    required = true,
                    value = "trigramme",
                    dataType = "string"),
            @ApiImplicitParam(name = "releaseType",
                    paramType = "path",
                    required = false,
                    value = "releases/snapshots/all",
                    dataType = "string"),
            @ApiImplicitParam(name = "format",
                    paramType = "query",
                    required = false,
                    value = "text/json",
                    dataType = "string")
    ])
    def showVersions() {
        String app = params.app
        String releaseType = params.releaseType ?: 'all'
        String format = params.format ?: 'json'

        log.info "searching for the list of delivery-notes with app=${app}, releaseType=${releaseType}..."
        def versions = nexusConsumerService.getVersions(app, releaseType)
        log.info "received the list of delivery-notes"

        if (format.toUpperCase() == 'TEXT')
            render versions.join(' ')
        else render versions as JSON
    }

    @ApiOperation(
            value = "Récupèrer une note de livraison",
            nickname = "deliveryNotes/{app}/{version}",
            produces = "application/json",
            consumes = "application/json",
            httpMethod = "GET"
    )
    @ApiImplicitParams([
            @ApiImplicitParam(name = "app",
                    paramType = "path",
                    required = true,
                    value = "app name",
                    dataType = "string"),
            @ApiImplicitParam(name = "version",
                    paramType = "path",
                    required = true,
                    value = "app version",
                    dataType = "string"),
            @ApiImplicitParam(name = "format",
                    paramType = "query",
                    required = false,
                    value = "result format",
                    dataType = "string")
    ])
    def showDn() {
        String app = params.app
        String version = params.version
        String format = params.format ?: 'json'

        log.info "searching for the delivery-note with app=${app}, version=${version}..."
        def resp = nexusConsumerService.getDn(app, version)
        log.info "received the delivery-note"

        //when there is no result
        if (resp.responseEntity.statusCode.toString() == '404') {
            String dnUrl = getNexusConsumerService().getDnUrl(app, version)
            String message = "No result for app=${app}, version=${version} !\nTried with url: ${dnUrl}"
            render message
            return
        }

        if (format.toUpperCase() == 'TEXT')
            render resp.text
        else render resp.json
    }

    @ApiOperation(
            value = "Valider une note de livraison stockée",
            nickname = "validations/{app}/{version}",
            produces = "application/json",
            consumes = "application/json",
            httpMethod = "GET"
    )
    @ApiResponses([
            @ApiResponse(code = 404,
                    message = "NDL Not Found"),
            @ApiResponse(code = 200,
                    message = "NDL validée"),
            @ApiResponse(code = 422,
                    message = "NDL Non Validé")])
    @ApiImplicitParams([
            @ApiImplicitParam(name = "app",
                    paramType = "path",
                    required = true,
                    value = "app name",
                    dataType = "string"),
            @ApiImplicitParam(name = "version",
                    paramType = "path",
                    required = true,
                    value = "app version",
                    dataType = "string")
    ])
    def validationStored() {
        String app = params.app
        String version = params.version

        log.info "searching for the delivery-note with app=${app}, version=${version}..."
        def resp = nexusConsumerService.getDn(app, version)
        log.info "received the delivery-note"

        //when there is no result
        if (resp.responseEntity.statusCode.toString() == '404') {
            String dnUrl = getNexusConsumerService().getDnUrl(app, version)
            response.status = 404
            String message = "No result for app=${app}, version=${version} !\nTried with url: ${dnUrl}"
            render message
            return
        }

        String dn = resp.text
        String schema = jsonSchemaValidationService.getSchemaText()

        def resValidation = jsonSchemaValidationService.validateSchema(schema, dn)

        setStatus(resValidation)
        render text: resValidation.toString(), contentType: 'application/json'
    }

    @ApiOperation(
            value = "Valider une note de livraison non stockée",
            nickname = "validations",
            produces = "application/json",
            consumes = "application/json",
            httpMethod = "POST"
    )
    @ApiResponses([
            @ApiResponse(code = 404,
                    message = "NDL Not Found"),
            @ApiResponse(code = 200,
                    message = "NDL validée"),
            @ApiResponse(code = 422,
                    message = "NDL Non Validé")])
    @ApiImplicitParams([
            @ApiImplicitParam(name = "dn",
                    paramType = "formData",
                    required = true,
                    value = "required dn content",
                    dataType = "string")
    ])
    def validationNoStored() {
        String dn = params.dn ?: ''
        String schema = jsonSchemaValidationService.getSchemaText()

        def res = jsonSchemaValidationService.validateSchema(schema, dn)

        setStatus(res)
        render text: res.toString(), contentType: 'application/json'
    }

    def setStatus(res) {
        if (res['valid']) response.status = 200
        else {
            if (!res['valid']) response.status = 422
            else response.status = 500
        }
    }

    /**
     * ref: https://support.sonatype.com/hc/en-us/articles/213465818-How-can-I-programmatically-upload-an-artifact-into-Nexus-2-
     * authentification a utiliser pour le moment : jenkins_nexus/Bb&fX!Z9
     * Prevoir un moyen de passer l’authentification dans l’appel REST entrant du DNrider.
     */
    @ApiOperation(
            value = "Stocker une note de livraison",
            nickname = "deliveryNotes/{app}/{releaseType}",
            produces = "application/json",
            consumes = "application/json",
            httpMethod = "POST"
    )
    @ApiImplicitParams([
            @ApiImplicitParam(name = "dn",
                    paramType = "formData",
                    required = true,
                    value = "required dn content",
                    dataType = "string"),
            @ApiImplicitParam(name = "app",
                    paramType = "path",
                    required = true,
                    value = "app name",
                    dataType = "string"),
            @ApiImplicitParam(name = "releaseType",
                    paramType = "path",
                    required = true,
                    value = "releases/snapshots",
                    dataType = "string"),
            @ApiImplicitParam(name = "version",
                    paramType = "query",
                    required = true,
                    value = "app version",
                    dataType = "string")
    ])
    def saveDn() {
        def dn = params.dn
        String app = params.app
        String releaseType = params.releaseType
        String version = params.version

        String repo = nexusConsumerService.getRepo(app, releaseType)

        def f = new File('temp')
        f.append dn.bytes

        String url = "http://nexus:50080/nexus/service/local/artifact/maven/content"
        def rest = new RestBuilder()
        def resp = rest.post(url) {
            auth 'jenkins_nexus', 'Bb&fX!Z9'
            contentType "multipart/form-data"
            r = repo
            hasPom = false
            e = 'json'
            g = "com.vsct." + app
            a = 'delivery-notes'
            p = 'json'
            v = version
            file = f
        }

        f.delete()

        //return 405 when the target is a Maven SNAPSHOT repository
        //when the version contain 'SNAPSHOT', it will be put in the snapshots repo automatically
        if (resp.status == 400) render status: 405, text: 'This is a Maven SNAPSHOT repository, and manual upload against it is forbidden!'
        else
            render status: 200, json: resp.json
    }

    /**
     * ref: https://stackoverflow.com/questions/34115434/how-to-delete-artifacts-with-classifier-from-nexus-using-rest-api
     * ce qu'il faut supprimer comme metadata ?
     */
    @ApiOperation(
            value = "Supprimer une note de livraison",
            nickname = "deliveryNotes/{app}/{version}",
            produces = "application/json",
            consumes = "application/json",
            httpMethod = "DELETE"
    )
    @ApiImplicitParams([
            @ApiImplicitParam(name = "app",
                    paramType = "path",
                    required = true,
                    value = "app name",
                    dataType = "string"),
            @ApiImplicitParam(name = "version",
                    paramType = "path",
                    required = true,
                    value = "app version",
                    dataType = "string")
    ])
    def deleteDn() {
        String app = params.app
        String version = params.version

        String url = "http://nexus:50080/nexus/service/local/repositories/asset-releases/content/com/vsct/${app}/delivery-notes/${version}/delivery-notes-${version}.json"
        def rest = new RestBuilder()
        def resp = rest.delete(url) {
            auth 'jenkins_nexus', 'Bb&fX!Z9'
        }

        if (resp.status == 204) {
            render status: 200, text: 'Dn Deleted'
        } else if (resp.status == 404) {
            render status: 404, text: 'Dn Not Found'
        } else render status: 400, text: 'Failed'
    }
}