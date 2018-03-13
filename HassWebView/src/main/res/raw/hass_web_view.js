var HassWebView = {

    /* If true, the user is an administrator, false otherwise */
    _admin: false,

    /* Because I don't know when all elements are really loaded, I'll be checking it within an interval */
    _retryOnLoadTimeout: null,

    /* If true, onLoaded event has been fired, false otherwise */
    _isLoaded: false,

    /* If true, a more info dialog is visible. False otherwise */
    _moreInfoDialogVisible: false,

    /* Fires when Android event onBackPressed is fired. Returns true if event is handled. False otherwise. */
    onBackPressed: function(){
        // If a dialog is visible -> Close it
        if (this.closeMoreInfoDialog()){
            return true;
        }

        // If "Overview" screen is not displayed, display it
        if (location.href.indexOf("/states") == -1){
            this.getDrawerItem("states").click();
            return true;
        }

        return false;
    },

    /* Closes a More Info dialog if it's visible. Returns true if it was visible, false otherwise */
    closeMoreInfoDialog: function(){
        if (this.isMoreInfoDialogVisible()){
            history.back();
            return true;
        }

        return false;
    },

    /* Hides an item from drawer menu */
    showDrawerItem: function(name, show){
        this.getDrawerItem(name).style.display = show ? "" : "none";
    },

    /* Returns an item from drawer menu */
    getDrawerItem: function(name){
        return document.querySelector("home-assistant").shadowRoot.querySelector("home-assistant-main").shadowRoot.querySelector("#drawer").querySelector("ha-sidebar").shadowRoot.querySelector("paper-listbox > paper-icon-item[data-panel='" + name + "']");
    },

    /* Hides developer tools from drawer menu */
    showDeveloperTools: function(show){
        document.querySelector("home-assistant").shadowRoot.querySelector("home-assistant-main").shadowRoot.querySelector("#drawer").querySelector("ha-sidebar").shadowRoot.querySelector("div.dev-tools").parentNode.style.display = show ? "" : "none";
    },

    /* Returns More Info Dialog element */
    getMoreInfoDialog: function(){
        return document.querySelector("home-assistant").shadowRoot.querySelector("home-assistant-main").shadowRoot.querySelector("more-info-dialog").shadowRoot.querySelector("paper-dialog");
    },

    /* Returns true if a "more info" dialog is visible. False otherwise */
    isMoreInfoDialogVisible: function(){
        return this.getMoreInfoDialog().style.display != "none";
    },

    /* Fired when more info dialog style changes  */
    onMoreInfoDialogStyleChanged: function(){
        var nowDialogIsVisible = this.isMoreInfoDialogVisible();

        if (!this._moreInfoDialogVisible && nowDialogIsVisible){
            HassWebView_EventHandler.onShowMoreInfoDialog();
        }
        else if (this._moreInfoDialogVisible && !nowDialogIsVisible){
            HassWebView_EventHandler.onHideMoreInfoDialog();
        }

        this._moreInfoDialogVisible = nowDialogIsVisible;
    },

    /* Observe info dialog style changes */
    observeOnMoreInfoDialogStyleChanges: function(){
        var that  = this;
        var observer = new MutationObserver(function(mutations) {
            mutations.forEach(function(mutationRecord) {
                that.onMoreInfoDialogStyleChanged();
            });
        });
        observer.observe(this.getMoreInfoDialog(), { attributes : true, attributeFilter : ["style"] });
    },

    /* Returns true if the user is an administrator, false otherwise */
    isAdmin: function(){
        return _admin;
    },

    /* Executed when webview loads the page */
    onLoad: function(isAdmin){
        if (this._retryOnLoadTimeout != null){
            clearTimeout(this._retryOnLoadTimeout);
            this._retryOnLoadTimeout = null;
        }

        if (this._isLoaded){
            return;
        }

        try{
            this.setAdmin(isAdmin);
            this.observeOnMoreInfoDialogStyleChanges();
            this._isLoaded = true;
        }catch(err){
            // Elements are not available yet, we will try again after some time
            var that = this;
            this._retryOnLoadTimeout = setTimeout(function(){
                that.onLoad(isAdmin);
            }, 200);
        }
    },

    /* Sets the user as admin, or not, and sets drawer menu items visibility */
    setAdmin: function(isAdmin){
        this.showDrawerItem("logbook", isAdmin);
        this.showDrawerItem("config", isAdmin);
        this.showDrawerItem("logout", isAdmin);
        this.showDeveloperTools(isAdmin);
        this._admin = isAdmin;
    }

};