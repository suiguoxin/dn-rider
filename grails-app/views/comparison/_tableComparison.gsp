<table class="table table-hover table-responsive">
    <thead>
    <tr>
        <g:if test="${versions}">
            <th>module</th>
            <th>name</th>
            <g:each var="version" in="${versions}">
                <th>${version}</th>
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
                <g:if test="${versions}">
                    <g:each var="version" in="${versions}">
                        <g:if test="${rowPackage[version]?.tag == 'deleted'}">
                            <td>
                                <span class="badge badge-danger">Deleted</span>
                            </td>
                        </g:if>
                        <g:else>
                            <td>
                                <a href="${rowPackage[version]?.packageUrl}">${rowPackage[version]?.name}</a>
                                <g:if test="${rowPackage[version]?.tag == 'new'}">
                                    <span class="badge badge-success">New</span>
                                </g:if>
                                <g:if test="${rowPackage[version]?.tag == 'changed'}">
                                    <span class="badge badge-info">!</span>
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