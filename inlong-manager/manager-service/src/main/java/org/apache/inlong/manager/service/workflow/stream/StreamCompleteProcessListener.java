/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.inlong.manager.service.workflow.stream;

import lombok.extern.slf4j.Slf4j;
import org.apache.inlong.manager.common.enums.EntityStatus;
import org.apache.inlong.manager.common.enums.SourceState;
import org.apache.inlong.manager.common.exceptions.WorkflowListenerException;
import org.apache.inlong.manager.common.pojo.workflow.form.GroupResourceProcessForm;
import org.apache.inlong.manager.dao.mapper.SourceFileDetailEntityMapper;
import org.apache.inlong.manager.service.core.InlongGroupService;
import org.apache.inlong.manager.service.core.InlongStreamService;
import org.apache.inlong.manager.service.source.StreamSourceService;
import org.apache.inlong.manager.workflow.WorkflowContext;
import org.apache.inlong.manager.workflow.event.ListenerResult;
import org.apache.inlong.manager.workflow.event.process.ProcessEvent;
import org.apache.inlong.manager.workflow.event.process.ProcessEventListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Event listener for completed creation of inlong stream resource
 */
@Slf4j
@Component
public class StreamCompleteProcessListener implements ProcessEventListener {

    @Autowired
    private InlongGroupService groupService;
    @Autowired
    private InlongStreamService streamService;
    @Autowired
    private StreamSourceService sourceService;
    @Autowired
    private SourceFileDetailEntityMapper fileDetailMapper;

    @Override
    public ProcessEvent event() {
        return ProcessEvent.COMPLETE;
    }

    /**
     * The creation process ends normally, modify the status of inlong group and other related info.
     */
    @Override
    public ListenerResult listen(WorkflowContext context) throws WorkflowListenerException {
        GroupResourceProcessForm form = (GroupResourceProcessForm) context.getProcessForm();
        String groupId = form.getInlongGroupId();
        String streamId = form.getInlongStreamId();
        String applicant = context.getApplicant();

        // update inlong group status
        groupService.updateStatus(groupId, EntityStatus.GROUP_CONFIG_SUCCESSFUL.getCode(), applicant);
        // update inlong stream status
        streamService.updateStatus(groupId, streamId, EntityStatus.STREAM_CONFIG_SUCCESSFUL.getCode(), applicant);
        // update file data source status
        fileDetailMapper.updateStatusAfterApprove(groupId, streamId, EntityStatus.AGENT_ADD.getCode(), applicant);
        // Update stream source status
        sourceService.updateStatus(groupId, streamId, SourceState.TO_BE_ISSUED_ADD.getCode(), applicant);

        return ListenerResult.success();
    }

    @Override
    public boolean async() {
        return false;
    }

}
