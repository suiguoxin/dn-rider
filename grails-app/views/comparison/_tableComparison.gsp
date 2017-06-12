<table class="table table-hover table-responsive">
    <thead>
    <tr>
        <g:if test="${rowVersions}">
            <th>module</th>
            <th>name</th>
            <g:each var="rowVersion" in="${rowVersions}">
                <th>
                    <a href="${rowVersion.url}" target="_blank">${rowVersion.name}</a>
                </th>
            </g:each>
        </g:if>
    </tr>
    </thead>
    <tbody>
    <g:if test="${rowPackages}">
        <g:each status="i" var="rowPackage" in="${rowPackages}">
            <tr>
                <th>${rowPackage.key.module}</th>
                <th>${rowPackage.key.name}</th>
                <g:if test="${rowVersions}">
                    <g:each var="version" in="${rowVersions.name}">
                        <g:if test="${rowPackage[version]?.tag == 'deleted'}">
                            <td class="text-nowrap">
                                <span class="badge badge-danger">
                                    <span class="fa fa-close" aria-hidden="true"></span>
                                </span>
                            </td>
                        </g:if>
                        <g:else>
                            %{--add nowrap class to prevent new line in table cell--}%
                            <td class="text-nowrap">
                                <g:if test="${rowPackage[version]?.tag == 'new'}">
                                    <span class="badge badge-success">
                                        <span class="fa fa-plus" aria-hidden="true"></span>
                                    </span>
                                </g:if>
                                <g:if test="${rowPackage[version]?.tag == 'changed'}">
                                    <span class="badge badge-info">
                                        <span class="fa fa-repeat" aria-hidden="true"></span>
                                    </span>
                                </g:if>
                                <g:if test="${rowPackage[version]?.name}">
                                    <g:if test="${rowPackage[version]?.url}">
                                        <a href="${rowPackage[version].url}" target="_blank" data-toggle="popover"
                                           data-title="${rowPackage.key?.name + ' / ' + rowPackage[version]?.name}"
                                           data-content="${rowPackage[version]?.content}">${rowPackage[version]?.name}</a>
                                    </g:if>
                                    <g:else>
                                        <a data-toggle="popover"
                                           data-title="${rowPackage.key?.name + ' / ' + rowPackage[version]?.name}"
                                           data-content="${rowPackage[version]?.content}">${rowPackage[version]?.name}</a>
                                    </g:else>
                                </g:if>
                            </td>
                        </g:else>
                    </g:each>
                </g:if>
            </tr>
        </g:each>
    </g:if>
    </tbody>
</table>