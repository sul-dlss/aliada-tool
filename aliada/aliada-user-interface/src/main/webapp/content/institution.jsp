<%@ page contentType="text/html;charset=UTF-8" language="java"
	pageEncoding="UTF-8"%>
<%@ taglib uri="/struts-tags" prefix="html"%>
<%@ taglib prefix="sj" uri="/struts-jquery-tags"%>

<link type="text/css" rel="stylesheet" href="<html:url value="css/institution.css" />" />

<script type="text/javascript" charset="UTF-8">	

function confirmBox(){
	var answer = window.confirm("<html:text name='edit.message'/>");
	if (answer == true) {
		console.log("Edit institution");
		$("#institutionConfiguration").submit();
	} else {
		console.log("Edit institution cancel");
	}
return false;
}

</script>

	<div id="institutionPage">
		<ul class="breadcrumb">
			<span class="breadCrumb"><html:text name="home"/></span>
			<li><span class="breadcrumb activeGreen"><html:text name="organisation.title"/></span></li>
		</ul>
			
			<div class="institutionPage">
				<html:form id="institutionConfiguration" action="/editInstitution.action" enctype="multipart/form-data">
				
					<div class="center">
						<img class="logo" src="<html:url action='ImageAction'/> " alt="userLogo"/>
					</div>
					
					<div class="row label">
						<html:textfield theme="xhtml" key="organisationName"
											cssClass="inputPage disabled" readonly="true"/>
					</div>	
					
					<div class="row label">				
						<html:file theme="xhtml" cssClass="inputPage" key="organisationLogo" />
					</div>		
					
					<div class="row label">
						<html:textfield theme="xhtml" key="organisationCatalogUrl"
											cssClass="inputPage" maxLength="128" />
					</div>
											
					<html:actionmessage/>
					<html:actionerror/>
					
				</html:form>
				
				<div class="buttons">
					<html:form>
			    		<html:submit cssClass="submitButton buttonGreen" key="save" onclick="return confirmBox();"></html:submit>
			    		<html:a action="manage" cssClass="fleft"><img alt="help" src="images/back.png"></img></html:a>
					</html:form>
				</div>	
			</div>	
	</div>