<%--
  ~ Copyright 2018 ABSA Group Limited
  ~
  ~ Licensed under the Apache License, Version 2.0 (the "License");
  ~ you may not use this file except in compliance with the License.
  ~ You may obtain a copy of the License at
  ~     http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing, software
  ~ distributed under the License is distributed on an "AS IS" BASIS,
  ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~ See the License for the specific language governing permissions and
  ~ limitations under the License.
  --%>

<!DOCTYPE HTML>
<html>
<head>
<meta http-equiv="X-UA-Compatible" content="IE=edge">
<meta http-equiv='Content-Type' content='text/html;charset=UTF-8' />
<meta name="_csrf" content="${_csrf.token}" />
<meta name="_csrf_header" content="${_csrf.headerName}" />

<link type="text/css" href="css/style.css" rel="stylesheet" />

<script src="webjars/openui5/sap-ui-core.js" id="sap-ui-bootstrap"
	data-sap-ui-libs="sap.m" data-sap-ui-bindingSyntax="complex"
	data-sap-ui-theme='sap_belize'>
</script>

<script src="generic/model.js"></script>
<script src="generic/prop.js"></script>
<script src="service/GenericService.js"></script>
<script src="service/SchemaService.js"></script>
<script src="service/DatasetService.js"></script>
<script src="service/MappingTableService.js"></script>
<script src="generic/functions.js"></script>
<script src="generic/formatters.js"></script>

<!-- only load the mobile lib "sap.m" and the "sap_bluecrystal" theme -->

<script>
	sap.ui.localResources("components");

	var component = new sap.ui.core.ComponentContainer("MenasComponent", {
		name : "components",
	});

	component.placeAt("content");
	
	component.setBusy(true);
</script>

</head>
<body class="sapUiBody" role="application">
	<div id="content"></div>
</body>
</html>