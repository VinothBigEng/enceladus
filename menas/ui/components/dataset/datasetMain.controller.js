/*
 * Copyright 2018 ABSA Group Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
jQuery.sap.require("sap.m.MessageBox");

sap.ui.controller("components.dataset.datasetMain", {

    /**
     * Called when a controller is instantiated and its View controls (if
     * available) are already created. Can be used to modify the View before it
     * is displayed, to bind event handlers and do other one-time
     * initialization.
     *
     * @memberOf components.dataset.datasetMain
     */
    onInit : function() {
        this._model = sap.ui.getCore().getModel();
        this._router = sap.ui.core.UIComponent.getRouterFor(this);
        this._router.getRoute("datasets").attachMatched(function(oEvent) {
            var arguments = oEvent.getParameter("arguments");
            this.routeMatched(arguments);
        }, this);

        this._addDialog = sap.ui.xmlfragment("components.dataset.addDataset", this);
        sap.ui.getCore().byId("newDatasetAddButton").attachPress(this.datasetAddSubmit, this);
        sap.ui.getCore().byId("newDatasetCancelButton").attachPress(this.datasetAddCancel, this);
        sap.ui.getCore().byId("newDatasetName").attachChange(this.datasetNameChange, this);
        sap.ui.getCore().byId("newDatasetSchemaNameSelect").attachChange(this.schemaSelect, this);
        sap.ui.getCore().byId("newDatasetHdfsRawSelector").attachToggleOpenState(this.hdfsPathOpen, this);
        sap.ui.getCore().byId("newDatasetHdfsRawSelector").attachSelectionChange(this.hdfsPathSelect, this);
        sap.ui.getCore().byId("newDatasetHdfsPublishSelector").attachToggleOpenState(this.hdfsPathOpen, this);
        sap.ui.getCore().byId("newDatasetHdfsPublishSelector").attachSelectionChange(this.hdfsPublishPathSelect, this);
        this._addDialog.setBusyIndicatorDelay(0)
    },

    toSchema : function(oEv) {
        var src = oEv.getSource();
        sap.ui.core.UIComponent.getRouterFor(this).navTo("schemas", {
            id : src.data("name"),
            version : src.data("version")
        })
    },

    fetchSchema : function(oEv) {
        var dataset = sap.ui.getCore().getModel().getProperty("/currentDataset");
        if (typeof (dataset.schema) === "undefined") {
            SchemaService.getSchemaVersion(dataset.schemaName, dataset.schemaVersion, "/currentDataset/schema")
        }
    },

    datasetAddCancel : function() {
        // This is a workaround for a bug in the Tree component of 1.56.5
        // TODO: verify whether this was fixed in the subsequent versions
        var tree = sap.ui.getCore().byId("newDatasetHdfsRawSelector");
        var items = tree.getItems();
        for ( var i in items) {
            items[i].setSelected(false)
        }

        var treePbulish = sap.ui.getCore().byId("newDatasetHdfsPublishSelector");
        var itemsPublish = treePbulish.getItems();
        for ( var i in itemsPublish) {
            itemsPublish[i].setSelected(false)
        }

        this.resetNewDatasetValueState();
        this._addDialog.close();
    },

    datasetAddSubmit : function() {
        var newDataset = this._model.getProperty("/newDataset");
        // we may wanna wait for a call to determine whether this is unique
        if (!newDataset.isEdit && newDataset.name && typeof (newDataset.nameUnique) === "undefined") {
            // need to wait for the service call
            setTimeout(this.datasetAddSubmit.bind(this), 500);
            return;
        }
        if (this.validateNewDataset()) {
            // send and update UI
            if (newDataset.isEdit) {
                DatasetService.editDataset(newDataset.name, newDataset.version, newDataset.description, newDataset.hdfsPath, newDataset.hdfsPublishPath, newDataset.schemaName, newDataset.schemaVersion)
            } else {
                DatasetService.createDataset(newDataset.name, newDataset.description, newDataset.hdfsPath, newDataset.hdfsPublishPath, newDataset.schemaName, newDataset.schemaVersion)
            }
            this.datasetAddCancel(); // close & clean up
        }
    },

    resetItems : function(items) {
        for ( var ind in items) {
            items[ind].setHighlight(sap.ui.core.MessageType.None)
        }
    },

    resetNewDatasetValueState : function() {
        sap.ui.getCore().byId("newDatasetName").setValueState(sap.ui.core.ValueState.None);
        sap.ui.getCore().byId("newDatasetName").setValueStateText("");

        sap.ui.getCore().byId("newDatasetSchemaNameSelect").setValueState(sap.ui.core.ValueState.None);
        sap.ui.getCore().byId("newDatasetSchemaNameSelect").setValueStateText("");

        sap.ui.getCore().byId("newDatasetSchemaVersionSelect").setValueState(sap.ui.core.ValueState.None);
        sap.ui.getCore().byId("newDatasetSchemaVersionSelect").setValueStateText("");

        this.resetItems(sap.ui.getCore().byId("newDatasetHdfsRawSelector").getItems());
        this.resetItems(sap.ui.getCore().byId("newDatasetHdfsPublishSelector").getItems());
    },

    schemaSelect : function(oEv) {
        var sSchemaId = oEv.getParameter("selectedItem").getKey();
        SchemaService.getAllSchemaVersions(sSchemaId, sap.ui.getCore().byId("newDatasetSchemaVersionSelect"))
    },

    hdfsPathOpen : function(oEv) {
        if (oEv.getParameter("expanded")) {
            var context = oEv.getParameter("itemContext").getPath();
            var path = this._model.getProperty(context).path;
            if (path !== "/")
                GenericService.hdfsList(path, context, this._addDialog)
        }
    },

    hdfsPathSelect : function(oEv) {
        var sBindingPath = oEv.getParameter("listItem").getBindingContext().getPath();
        var sHdfsPath = this._model.getProperty(sBindingPath).path;
        this._model.setProperty("/newDataset/hdfsPath", sHdfsPath)
    },

    hdfsPublishPathSelect : function(oEv) {
        var sBindingPath = oEv.getParameter("listItem").getBindingContext().getPath();
        var sHdfsPath = this._model.getProperty(sBindingPath).path;
        this._model.setProperty("/newDataset/hdfsPublishPath", sHdfsPath)
    },

    tabSelect : function(oEv) {
        if (oEv.getParameter("selectedKey") === "schema")
            this.fetchSchema();
    },

    _loadAllVersionsOfFirstSchema : function() {
        this._model.setProperty("/newDataset", {
            isEdit : false,
            title : "Add"
        });

        var schemas = this._model.getProperty("/schemas");

        if (schemas.length > 0) {
            this._model.setProperty("/newSchema", {
                schemaName : schemas[0]._id
            });

            var sSchema = this._model.getProperty("/schemas/0/_id");
            SchemaService.getAllSchemaVersions(sSchema, sap.ui.getCore().byId("newDatsetSchemaVersionSelect"))
        }
    },

    validateNewDataset : function() {
        this.resetNewDatasetValueState();
        var oDataset = this._model.getProperty("/newDataset");
        var isOk = true;

        if (!oDataset.name || oDataset.name === "") {
            sap.ui.getCore().byId("newDatasetName").setValueState(sap.ui.core.ValueState.Error);
            sap.ui.getCore().byId("newDatasetName").setValueStateText("Dataset name cannot be empty");
            isOk = false;
        }
        if (!oDataset.isEdit && !oDataset.nameUnique) {
            sap.ui.getCore().byId("newDatasetName").setValueState(sap.ui.core.ValueState.Error);
            sap.ui.getCore().byId("newDatasetName").setValueStateText(
                "Dataset name '" + oDataset.name + "' already exists. Choose a different name.");
            isOk = false;
        }
        if (!oDataset.schemaName || oDataset.schemaName === "") {
            sap.ui.getCore().byId("newDatasetSchemaNameSelect").setValueState(sap.ui.core.ValueState.Error);
            sap.ui.getCore().byId("newDatasetSchemaNameSelect").setValueStateText("Please choose the schema of the dataset");
            isOk = false;
        }
        if (oDataset.schemaVersion === undefined || oDataset.schemaVersion === "") {
            sap.ui.getCore().byId("newDatasetSchemaVersionSelect").setValueState(sap.ui.core.ValueState.Error);
            sap.ui.getCore().byId("newDatasetSchemaVersionSelect").setValueStateText("Please choose the version of the schema for the dataset");
            isOk = false;
        }
        if (!oDataset.hdfsPath || oDataset.hdfsPath === "") {
            var items = sap.ui.getCore().byId("newDatasetHdfsRawSelector").getItems();
            for ( var ind in items) {
                items[ind].setHighlight(sap.ui.core.MessageType.Error)
            }
            sap.m.MessageToast.show("Please choose the raw HDFS path of the dataset");
            isOk = false;
        }
        if (!oDataset.hdfsPublishPath || oDataset.hdfsPublishPath === "") {
            var items = sap.ui.getCore().byId("newDatasetHdfsPublishSelector").getItems();
            for ( var ind in items) {
                items[ind].setHighlight(sap.ui.core.MessageType.Error)
            }
            sap.m.MessageToast.show("Please choose the publish HDFS path of the dataset");
            isOk = false;
        }

        return isOk;
    },

    onAddPress : function() {
        GenericService.hdfsList("/", "/newDataset/HDFSRaw", this._addDialog);
        GenericService.hdfsList("/", "/newDataset/HDFSPublish", this._addDialog);

        this._loadAllVersionsOfFirstSchema();

        sap.ui.getCore().byId("newDatasetHdfsRawSelector").expandToLevel(1);
        sap.ui.getCore().byId("newDatasetHdfsPublishSelector").expandToLevel(1);

        this._addDialog.open();
    },

    datasetNameChange : function() {
        DatasetService.isUniqueDatasetName(this._model.getProperty("/newDataset/name"))
    },

    onEditPress : function() {
        var current = this._model.getProperty("/currentDataset");

        current.isEdit = true;
        current.title = "Edit";

        this._model.setProperty("/newDataset", current);

        SchemaService.getAllSchemaVersions(current.schemaName, sap.ui.getCore().byId("newDatasetSchemaVersionSelect"));

        this._addDialog.open();

        sap.ui.getCore().byId("newDatasetHdfsRawSelector").collapseAll();
        sap.ui.getCore().byId("newDatasetHdfsPublishSelector").collapseAll();

        GenericService.treeNavigateTo(current.hdfsPath, "/newDataset/HDFSRaw", "newDatasetHdfsRawSelector", this);
        GenericService.treeNavigateTo(current.hdfsPath, "/newDataset/HDFSPublish", "newDatasetHdfsPublishSelector", this);
    },

    onRemovePress : function(oEv) {
        var current = this._model.getProperty("/currentDataset");

        sap.m.MessageBox.show("This action will remove all versions of the dataset definition. \nAre you sure?.", {
            icon : sap.m.MessageBox.Icon.WARNING,
            title : "Are you sure?",
            actions : [sap.m.MessageBox.Action.YES, sap.m.MessageBox.Action.NO],
            onClose : function(oAction) {
                if (oAction === "YES") {
                    DatasetService.disableDataset(current.name)
                }
            }
        });
    },

    datasetSelected : function(oEv) {
        var selected = oEv.getParameter("listItem").data("id");
        this._router.navTo("datasets", {
            id : selected
        });
    },

	routeMatched : function(oParams) {
		if (Prop.get(oParams, "id") === undefined) {
			DatasetService.getDatasetList(true);
		} else if (Prop.get(oParams, "version") === undefined) {
			DatasetService.getDatasetList();
			DatasetService.getLatestDatasetVersion(oParams.id)
		} else {
			DatasetService.getDatasetList();
			DatasetService.getDatasetVersion(oParams.id, oParams.version)
		}
	},

/**
 * Similar to onAfterRendering, but this hook is invoked before the controller's
 * View is re-rendered (NOT before the first rendering! onInit() is used for
 * that one!).
 * 
 * @memberOf components.dataset.datasetMain
 */
// onBeforeRendering: function() {
//
// },
/**
 * Called when the View has been rendered (so its HTML is part of the document).
 * Post-rendering manipulations of the HTML could be done here. This hook is the
 * same one that SAPUI5 controls get after being rendered.
 * 
 * @memberOf components.dataset.datasetMain
 */
    onAfterRendering : function() {
        // get schemas after rendering. This will be used for add/edit
        // functionality
        var schemas = this._model.getProperty("/schemas");
        if (!schemas || schemas.length === 0) {
            SchemaService.getSchemaList(false, true);
        }
    }
/**
 * Called when the Controller is destroyed. Use this one to free resources and
 * finalize activities.
 * 
 * @memberOf components.dataset.datasetMain
 */
// onExit: function() {
//
// }
});