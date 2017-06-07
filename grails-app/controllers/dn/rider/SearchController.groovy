package dn.rider

import grails.converters.JSON

class SearchController {

    def nexusConsumerService

    def index() {
        def apps = nexusConsumerService.getApps()
        [apps: apps as JSON]
    }

    def getVersionsList() {
        String app = params.app
        String releaseType = params.releaseType

        log.info "searching for the list of delivery-notes with app=${app}, releaseType=${releaseType}..."
        def versions = nexusConsumerService.getVersions(app, releaseType)
        log.info "received the list of delivery-notes"

        render versions as JSON
    }

    def getVersionsView() {
        String app = params.app
        String releaseType = params.releaseType
        String template = params.template

        log.info "searching for the list of delivery-notes with app=${app}, releaseType=${releaseType}..."
        def versions = nexusConsumerService.getVersions(app, releaseType)
        log.info "received the list of delivery-notes"

        render template: template, model: [versions: versions, versionCount: versions.size(), app: app, releaseType: releaseType]
    }

    def search(SearchCommand cmd) {
        //take the parameters from the object command
        String app = cmd.app
        String version = cmd.version
        String releaseType = cmd.releaseType

        if (cmd.hasErrors()) {
            def firstError = cmd.errors.allErrors[0]
            if (firstError.field == 'app') {
                flash.message = "The valid size range of field app is between 3 and 15"
            }
            respond([
                    app        : app,
                    releaseType: releaseType,
                    version    : version
            ], view: 'search')
            return
        }

        //search for the list of delivery-notes by using the service functioin
        log.info "searching for the list of delivery-notes with app=${app}, releaseType=${releaseType}..."
        def versions = nexusConsumerService.getVersions(app, releaseType)
        log.info "received the list of delivery-notes"

        //before the user choose the version
        if (!version) {
            respond([
                    versionCount: versions.size(),
                    versions    : versions,
                    app         : app,
                    releaseType : releaseType
            ], view: "search")
            return
        }
        //when the user choose the version
        else {
            log.info "searching for the delivery-note with app=${app}, version=${version}..."
            def resp = nexusConsumerService.getDn(app, version)
            log.info "received the delivery-note"

            //when there is no result
            if (resp.responseEntity.statusCode.toString() == '404') {
                String dnUrl = getNexusConsumerService().getDnUrl(app, version)
                flash.message = "No result for app=${app}, version=${version} !\nTried with url: ${dnUrl}"
                respond([
                        versions    : versions,
                        versionCount: versions.size(),
                        app         : app,
                        releaseType : releaseType,
                        version     : version
                ], view: "search")
                return
            }

            respond([
                    versions    : versions,
                    versionCount: versions.size(),
                    dnRaw       : resp.text,
                    dnJson      : resp.json,
                    app         : app,
                    releaseType : releaseType,
                    version     : version
            ], view: "search")
        }
    }
}