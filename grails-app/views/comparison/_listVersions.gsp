<g:each var="version" in="${versions}">
    <div class="form-check">
        <label class="form-check-label">
            <input class="form-check-input" type="checkbox" name="versions" value="${version}"> ${version}</label>
    </div>
</g:each>