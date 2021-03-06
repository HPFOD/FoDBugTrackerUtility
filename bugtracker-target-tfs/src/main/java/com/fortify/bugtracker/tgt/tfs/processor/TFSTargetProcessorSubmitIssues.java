/*******************************************************************************
 * (c) Copyright 2020 Micro Focus or one of its affiliates, a Micro Focus company
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a 
 * copy of this software and associated documentation files (the 
 * "Software"), to deal in the Software without restriction, including without 
 * limitation the rights to use, copy, modify, merge, publish, distribute, 
 * sublicense, and/or sell copies of the Software, and to permit persons to 
 * whom the Software is furnished to do so, subject to the following 
 * conditions:
 * 
 * The above copyright notice and this permission notice shall be included 
 * in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY 
 * KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE 
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR 
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, 
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF 
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN 
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS 
 * IN THE SOFTWARE.
 ******************************************************************************/
package com.fortify.bugtracker.tgt.tfs.processor;

import java.util.LinkedHashMap;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fortify.bugtracker.common.tgt.issue.ITargetIssueFieldsRetriever;
import com.fortify.bugtracker.common.tgt.issue.TargetIssueLocator;
import com.fortify.bugtracker.common.tgt.processor.AbstractTargetProcessorSubmitIssues;
import com.fortify.bugtracker.tgt.tfs.cli.ICLIOptionsTFS;
import com.fortify.bugtracker.tgt.tfs.config.TFSTargetConfiguration;
import com.fortify.bugtracker.tgt.tfs.connection.TFSConnectionFactory;
import com.fortify.bugtracker.tgt.tfs.connection.TFSRestConnection;
import com.fortify.processrunner.cli.CLIOptionDefinitions;
import com.fortify.processrunner.context.Context;
import com.fortify.util.rest.json.JSONMap;

/**
 * This {@link AbstractTargetProcessorSubmitIssues} implementation
 * submits issues to TFS.
 */
@Component
public class TFSTargetProcessorSubmitIssues extends AbstractTargetProcessorSubmitIssues {
	private String workItemType;
	
	@Override
	public void addTargetCLIOptionDefinitions(CLIOptionDefinitions cliOptionDefinitions) {
		TFSConnectionFactory.addCLIOptionDefinitions(cliOptionDefinitions);
		cliOptionDefinitions.add(ICLIOptionsTFS.CLI_TFS_COLLECTION);
		cliOptionDefinitions.add(ICLIOptionsTFS.CLI_TFS_PROJECT);
	}
	
	public String getTargetName() {
		return "TFS";
	}
	
	@Override
	protected TargetIssueLocator submitIssue(Context context, LinkedHashMap<String, Object> issueData) {
		TFSRestConnection conn = TFSConnectionFactory.getConnection(context);
		issueData.put("System.Title", StringUtils.abbreviate((String)issueData.get("System.Title"), 254));
		return conn.submitIssue(
			ICLIOptionsTFS.CLI_TFS_COLLECTION.getValue(context), 
			ICLIOptionsTFS.CLI_TFS_PROJECT.getValue(context), getWorkItemType(), issueData);
	}
	
	@Override
	protected ITargetIssueFieldsRetriever getTargetIssueFieldsRetriever() {
		return new ITargetIssueFieldsRetriever() {
			public JSONMap getIssueFieldsFromTarget(Context context, TargetIssueLocator targetIssueLocator) {
				return TFSConnectionFactory.getConnection(context)
						.getWorkItemFields(ICLIOptionsTFS.CLI_TFS_COLLECTION.getValue(context), targetIssueLocator);
			}
		};
	}

	public String getWorkItemType() {
		return workItemType;
	}

	public void setWorkItemType(String workItemType) {
		this.workItemType = workItemType;
	}
	
	@Autowired
	public void setConfiguration(TFSTargetConfiguration config) {
		setWorkItemType(config.getWorkItemType());
	}
	
}
