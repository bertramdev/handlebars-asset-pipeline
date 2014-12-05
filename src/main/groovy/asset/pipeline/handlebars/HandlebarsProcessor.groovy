/*
* Copyright 2014 the original author or authors.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package asset.pipeline.handlebars

import asset.pipeline.AssetHelper
import asset.pipeline.AssetFile
import asset.pipeline.AssetCompiler
import asset.pipeline.AssetPipelineConfigHolder
import org.mozilla.javascript.Context
import org.mozilla.javascript.Scriptable
import asset.pipeline.AbstractProcessor

class HandlebarsProcessor extends AbstractProcessor {

	Scriptable globalScope
	ClassLoader classLoader
	HandlebarsProcessor(AssetCompiler precompiler){
		super(precompiler)
		try {
			classLoader = getClass().getClassLoader()

			Context cx = Context.enter()
			cx.setOptimizationLevel(-1)
			globalScope = cx.initStandardObjects()
			loadHandlebars(cx)
		} catch (Exception e) {
			throw new Exception("Handlebars Engine initialization failed.", e)
		} finally {
			try {
				Context.exit()
			} catch (IllegalStateException e) {}
		}
	}

	public void loadHandlebars(Context cx) {
		String scanPath = AssetPipelineConfigHolder.config?.handlebars?.scanPath ?: 'handlebars.js'
		AssetFile handlebarsAssetFile = AssetHelper.fileForFullName(scanPath)

		if(handlebarsAssetFile) {
			cx.evaluateString globalScope, handlebarsAssetFile.inputStream.getText('UTF-8'), handlebarsAssetFile.name, 1, null
		} else {
			def handlebarsJsResource = classLoader.getResource('asset/pipeline/handlebars/handlebars.js')
			cx.evaluateString globalScope, handlebarsJsResource.getText('UTF-8'), handlebarsJsResource.file, 1, null
		}

	}

	String process(String input,AssetFile assetFile) {
		try {
			def cx = Context.enter()
			def compileScope = cx.newObject(globalScope)
			compileScope.setParentScope(globalScope)
			compileScope.put("handlebarsSrc", compileScope, input)
			def result = cx.evaluateString(compileScope, "Handlebars.precompile(handlebarsSrc)", "Handlebars compile command", 0, null)
			return wrapTemplate(templateNameForFile(assetFile), result)
		} catch (Exception e) {
			throw new Exception("""
			Handlebars Engine compilation of handlebars to javascript failed.
			$e
			""")
		} finally {
			Context.exit()
		}
	}

	def templateNameForFile(assetFile) {
		def templateRoot      = AssetPipelineConfigHolder.config?.handlebars?.templateRoot ?: 'templates'
		def templateSeperator = AssetPipelineConfigHolder.config?.handlebars?.templatePathSeperator ?: '/'

		def relativePath      = relativePath(assetFile.parentPath, templateRoot, templateSeperator)
		def templateName      = AssetHelper.nameWithoutExtension(assetFile.getName())
		if(relativePath) {
			templateName = [relativePath,templateName].join(templateSeperator)
		}
		return templateName
	}

	def relativePath(parentPath, templateRoot, templateSeperator) {
		def path          = parentPath.split("/")
		def startPosition = path.findLastIndexOf{ it == templateRoot }

		if(startPosition+1 >= path.length) {
			return ""
		}

		path = path[(startPosition+1)..-1]
		return path.join(templateSeperator)
	}

	def wrapTemplate = { String templateName, String compiledTemplate ->
		"""
		(function(){
			var template = Handlebars.template, templates = Handlebars.templates = Handlebars.templates || {};
				templates['$templateName'] = template($compiledTemplate);
				}());
				"""
	}
}
