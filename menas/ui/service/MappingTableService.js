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
var MappingTableService = new function() {

    var model = sap.ui.getCore().getModel();

    this.getMappingTableList = function(bLoadFirst, bGetSchema) {
	Functions.ajax("api/mappingTable/list", "GET", {}, function(oData) {
	    model.setProperty("/mappingTables", oData)
	    if (oData.length > 0 && bLoadFirst)
		MappingTableService.getMappingTableVersion(oData[0]._id, oData[0].latestVersion, bGetSchema)
	}, function() {
	    sap.m.MessageBox.error("Failed to get the list of mapping tables. Please wait a moment and try reloading the application")
	})
    };

    this.getLatestMappingTableVersion = function(sId, bGetSchema) {
	Functions.ajax("api/mappingTable/detail/" + encodeURI(sId) + "/latest", "GET", {}, function(oData) {
	    model.setProperty("/currentMappingTable", oData)
	    MappingTableService.getMappingTableUsedIn(oData.name, oData.version)
	    if (bGetSchema) {
		SchemaService.getSchemaVersion(oData.schemaName, oData.schemaVersion, "/currentMappingTable/schema")
	    }
	}, function() {
	    sap.m.MessageBox.error("Failed to get the detail of the mapping table. Please wait a moment and try reloading the application")
	    window.location.hash = "#/mapping"
	})
    };

    this.getMappingTableVersion = function(sId, iVersion, bGetSchema) {
	Functions.ajax("api/mappingTable/detail/" + encodeURI(sId) + "/" + encodeURI(iVersion), "GET", {}, function(oData) {
	    model.setProperty("/currentMappingTable", oData)
	    MappingTableService.getMappingTableUsedIn(oData.name, oData.version)
	    if (bGetSchema) {
		SchemaService.getSchemaVersion(oData.schemaName, oData.schemaVersion, "/currentMappingTable/schema")
	    }
	}, function() {
	    sap.m.MessageBox.error("Failed to get the detail of the mapping table. Please wait a moment and try reloading the application")
	    window.location.hash = "#/mapping"
	})
    };

    this.getMappingTableUsedIn = function(sId, iVersion) {
	Functions.ajax("api/mappingTable/usedIn/" + encodeURI(sId) + "/" + encodeURI(iVersion), "GET", {}, function(oData) {
	    model.setProperty("/currentMappingTable/usedIn", oData)
	}, function() {
	    sap.m.MessageBox.error("Failed to retreive the 'Used In' section, please try again later.")
	})
    };

    this.isUniqueMappingName = function(sName) {
	model.setProperty("/newMappingTable/nameUsed", undefined)
	Functions.ajax("api/mappingTable/isUniqueName/" + encodeURI(sName), "GET", {}, function(oData) {
	    model.setProperty("/newMappingTable/nameUnique", oData)
	}, function() {
	    sap.m.MessageBox.error("Failed to retreive isUniqueName. Please try again later.")
	})
    }

    this.createMappingTable = function(sName, sDescription, sHdfsPath, sSchemaName, iSchemaVersion) {
	Functions.ajax("api/mappingTable/create", "POST", {
	    name : sName,
	    description : sDescription,
	    hdfsPath : sHdfsPath,
	    schemaName : sSchemaName,
	    schemaVersion : iSchemaVersion
	}, function(oData) {
	    MappingTableService.getMappingTableList();
	    SchemaService.getSchemaVersion(oData.schemaName, oData.schemaVersion, "/currentMappingTable/schema")
	    model.setProperty("/currentMappingTable", oData)
	    sap.m.MessageToast.show("Mapping Table created.");
	}, function() {
	    sap.m.MessageBox.error("Failed to create the mapping table, try reloading the application or try again later.")
	})
    };

    this.editMappingTable = function(sName, iVersion, sDescription, sHdfsPath, sSchemaName, iSchemaVersion) {
	Functions.ajax("api/mappingTable/edit", "POST", {
	    name : sName,
	    version : iVersion,
	    description : sDescription,
	    hdfsPath : sHdfsPath,
	    schemaName : sSchemaName,
	    schemaVersion : iSchemaVersion
	}, function(oData) {
	    MappingTableService.getMappingTableList();
	    model.setProperty("/currentMappingTable", oData)
	    SchemaService.getSchemaVersion(oData.schemaName, oData.schemaVersion, "/currentMappingTable/schema")
	    sap.m.MessageToast.show("Mapping Table updated.");
	}, function() {
	    sap.m.MessageBox.error("Failed to create the mapping table, try reloading the application or try again later.")
	})
    };

    this.editDefaultValues = function(sName, iVersion, aDefaults) {
	Functions.ajax("api/mappingTable/updateDefaults", "POST", {
	    id : {
		name : sName,
		version : iVersion
	    },
	    value : aDefaults
	}, function(oData) {
	    MappingTableService.getMappingTableList();
	    model.setProperty("/currentMappingTable", oData)
	    SchemaService.getSchemaVersion(oData.schemaName, oData.schemaVersion, "/currentMappingTable/schema")
	    sap.m.MessageToast.show("Default values updated.");
	}, function() {
	    sap.m.MessageBox.error("Failed to update the default value, try reloading the application or try again later.")
	})
    };

    this.addDefault = function(sName, iVersion, oDefault) {
	Functions.ajax("api/mappingTable/addDefault", "POST", {
	    id : {
		name : sName,
		version : iVersion
	    },
	    value : {
		columnName : oDefault.columnName,
		value : oDefault.value
	    }
	}, function(oData) {
	    MappingTableService.getMappingTableList();
	    MappingTableService.getLatestMappingTableVersion(sName, true)
	    sap.m.MessageToast.show("Default value added.");
	}, function() {
	    sap.m.MessageBox.error("Failed add default value, try reloading the application or try again later.")
	})
    };

    this.disableMappingTable = function(sId, iVersion) {
	var uri = "api/mappingTable/disable/" + encodeURI(sId)
	if (typeof (iVersion) !== "undefined") {
	    uri += "/" + encodeURI(iVersion)
	}

	Functions.ajax(uri, "GET", {}, function(oData) {
	    if (Array.isArray(oData)) {
		var err = "Disabling mapping table failed. Clear the following dependencies first:\n";
		for ( var ind in oData) {
		    err += "\t - " + oData[ind].name + " (v. " + oData[ind].version + ")";
		}
		sap.m.MessageBox.error(err)
	    } else if (typeof (oData) === "object") {
		sap.m.MessageToast.show("Mapping table disabled.");
		if (window.location.hash != "#/mapping") {
		    window.location.hash = "#/mapping"
		} else {
		    MappingTableService.getMappingTableList(true, false)
		}
	    }
	}, function() {
	    sap.m.MessageBox.error("Failed to disable mapping table. Ensure no active datasets use this mapping table(and/or version)")
	})
    };

}();