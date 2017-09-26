/*******************************************************************************
 * Copyright (c) Intel Corporation
 * Copyright (c) 2017
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
 *******************************************************************************/
package org.osc.core.broker.service.tasks.conformance.k8s.securitygroup;

import java.util.Set;

import javax.persistence.EntityManager;

import org.apache.log4j.Logger;
import org.osc.core.broker.job.TaskGraph;
import org.osc.core.broker.job.lock.LockObjectReference;
import org.osc.core.broker.model.entities.virtualization.SecurityGroup;
import org.osc.core.broker.model.entities.virtualization.SecurityGroupMember;
import org.osc.core.broker.service.persistence.OSCEntityManager;
import org.osc.core.broker.service.tasks.TransactionalMetaTask;
import org.osc.core.broker.service.tasks.conformance.openstack.securitygroup.PortGroupCheckMetaTask;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(service = UpdateOrDeleteK8sSecurityGroupMetaTask.class)
public class UpdateOrDeleteK8sSecurityGroupMetaTask extends TransactionalMetaTask {
    private static final Logger LOG = Logger.getLogger(UpdateOrDeleteK8sSecurityGroupMetaTask.class);

    @Reference
    CheckK8sSecurityGroupLabelMetaTask checkK8sSecurityGroupLabelMetaTask;

    @Reference
    PortGroupCheckMetaTask portGroupCheckMetaTask;

    private SecurityGroup sg;

    private TaskGraph tg;

    public UpdateOrDeleteK8sSecurityGroupMetaTask create(SecurityGroup sg) {
        UpdateOrDeleteK8sSecurityGroupMetaTask task = new UpdateOrDeleteK8sSecurityGroupMetaTask();
        task.sg = sg;
        task.checkK8sSecurityGroupLabelMetaTask = this.checkK8sSecurityGroupLabelMetaTask;
        task.portGroupCheckMetaTask = this.portGroupCheckMetaTask;
        task.dbConnectionManager = this.dbConnectionManager;
        task.txBroadcastUtil = this.txBroadcastUtil;

        return task;
    }

    @Override
    public TaskGraph getTaskGraph() {
        return this.tg;
    }

    @Override
    public String getName() {
        final boolean isDelete = this.sg.getMarkedForDeletion();
        return String.format("%s Kubernetes Security Group %s", isDelete ? "Delete" : "Update",
                this.sg.getName());
    }

    @Override
    public void executeTransaction(EntityManager em) throws Exception {
        this.tg = new TaskGraph();
        OSCEntityManager<SecurityGroup> dsEmgr = new OSCEntityManager<SecurityGroup>(SecurityGroup.class, em, this.txBroadcastUtil);
        this.sg = dsEmgr.findByPrimaryKey(this.sg.getId());

        final boolean isDelete = this.sg.getMarkedForDeletion() == null ? false : this.sg.getMarkedForDeletion();

        String domainId = null;

        if (!this.sg.getSecurityGroupMembers().isEmpty()) {
            SecurityGroupMember sgm = this.sg.getSecurityGroupMembers().iterator().next();

            if (!sgm.getPodPorts().isEmpty()) {
                domainId = sgm.getPodPorts().iterator().next().getParentId();
            }
        }

        this.sg.getSecurityGroupMembers().forEach(sgm -> {
            this.tg.addTask(this.checkK8sSecurityGroupLabelMetaTask.create(sgm, isDelete));
        });

        if (domainId != null) {
            this.tg.appendTask(this.portGroupCheckMetaTask.create(this.sg,
                    isDelete, domainId));
        } else {
            LOG.info(String.format("This SG does not have any associated pods. Skipping port group sync."));
        }
    }

    @Override
    public Set<LockObjectReference> getObjects() {
        return LockObjectReference.getObjectReferences(this.sg);
    }
}