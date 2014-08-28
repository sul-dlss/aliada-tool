<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ taglib uri="/struts-tags" prefix="html" %>

<h2 class="pageTitle"><html:text name="linking.title"/></h2>
	<div id="form">	
		<div class="content">	
			<html:form>	
			<div <html:if test="notFiles||linkingStarted">class="displayNo"</html:if>
				<html:else>
				    class="display"
				</html:else>>
				<h3 class="bigLabel"><html:text name="linking.importedFiles"/></h3>
				<html:iterator value="filesToLink">
					<html:radio name="fileToLink" list="{value}"/><br/>
				</html:iterator>
			</div>
			<div <html:if test="notFiles">class="display"</html:if>
				<html:else>
				    class="displayNo"
				</html:else>>
				<h3 class="bigLabel"><html:text name="linking.notFiles"/></h3>
			</div>
			<div <html:if test="linkingStarted">class="display"</html:if>
				<html:else>
				    class="displayNo"
				</html:else>>
				<h3 class="bigLabel"><html:text name="linking.selectedFile"/></h3>
				<html:property value="fileToLink" />	
			</div>
			<h3 class="mediumLabel"><html:text name="linking.datasets"/></h3>		
			<html:iterator value="datasets" var="data">
	          <li><html:property value="value"/></li>
	       </html:iterator> 
			<html:actionerror/>
			<div class="row">
				<html:submit action="startLinking" cssClass="submitButton button" key="linkSubmit"/>
				<div <html:if test="showCheckButton">class="displayInline"</html:if>
					<html:else>
					    class="displayNo"
					</html:else>>
					<html:submit action="linkingInfo" cssClass="submitButton button" key="check"/>
				</div>
			</div>
			</html:form>
		</div>	
	</div>