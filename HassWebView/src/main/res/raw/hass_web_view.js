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
        if (location.href.indexOf("/lovelace/default_view") == -1){
            location.href = this.getDrawerItem("lovelace").getAttribute("href");
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

    /* Returns ha-sidebar element */
    getSideBar: function(){
        return document.querySelector("home-assistant").shadowRoot.querySelector("home-assistant-main").shadowRoot.querySelector("app-drawer-layout > #drawer").querySelector("ha-sidebar");
    },

    /* Returns an item from drawer menu */
    getDrawerItem: function(name){
        return this.getSideBar().shadowRoot.querySelector("paper-listbox > a[data-panel='" + name + "']");
    },

    /* Hides developer tools from drawer menu */
    showDeveloperTools: function(show){
        this.getSideBar().shadowRoot.querySelector("div.dev-tools").parentNode.style.display = show ? "" : "none";
    },

    /* Returns the profile link */
    showProfileLink: function(show){
        this.getSideBar().shadowRoot.querySelector("app-toolbar > a.profile-badge").style.display = show ? "" : "none";
    },

    /* Returns the parent of More Info Dialog element */
    getMoreInfoDialogParent: function(){
        return document.querySelector("home-assistant").shadowRoot;
    },

    /* Returns More Info Dialog element */
    getMoreInfoDialog: function(){
        return document.querySelector("home-assistant").shadowRoot.querySelector("ha-more-info-dialog");
    },

    /* Returns true if a "more info" dialog is visible. False otherwise */
    isMoreInfoDialogVisible: function(){
        var dialog = this.getMoreInfoDialog();
        return dialog && dialog.style.display != "none";
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

    isWaitingForMoreInfoDialog: false,

    /* Observe info dialog style changes */
    observeOnMoreInfoDialogStyleChanges: function(){
        // Only wait for more info dialog if it's not waiting yet
        if (!this.isWaitingForMoreInfoDialog){
            var dialogParent = this.getMoreInfoDialogParent();

            if (dialogParent == null){
                throw "Dialog parent not loaded yet!";;
            }
            else {
                this.isWaitingForMoreInfoDialog = true;
                var that  = this;

                // Wait for more info dialog to be added to DOM
                this.waitForAddedNode({
                    getElement: this.getMoreInfoDialog,
                    parent: dialogParent,
                    recursive: false,
                    done: function(el){
                        // Once the element (this.getMoreInfoDialog()) is added, observe style changes
                        var observer = new MutationObserver(function(mutations) {
                            mutations.forEach(function(mutationRecord) {
                                that.onMoreInfoDialogStyleChanged();
                            });
                        });
                        observer.observe(el, { attributes : true, attributeFilter : ["style"] });
                    }
                });
            }
        }
    },

    /* Waits for a node to be added */
    waitForAddedNode: function(params) {
        new MutationObserver(function(mutations) {
            var el = params.getElement();
            if (el) {
                this.disconnect();
                params.done(el);
            }
        }).observe(params.parent || document, {
            subtree: !!params.recursive,
            childList: true,
        });
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
        // this.showDrawerItem("logbook", isAdmin);
        // this.showDrawerItem("config", isAdmin);
        // this.showDeveloperTools(isAdmin);
        // this.showProfileLink(isAdmin);
        this._admin = isAdmin;
    }

};