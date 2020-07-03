/*
 * Copyright (c) 2008-2020, Hazelcast, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hazelcast.jet.sql.impl;

import com.hazelcast.jet.core.DAG;
import com.hazelcast.sql.SqlRowMetadata;
import com.hazelcast.sql.impl.QueryId;
import com.hazelcast.sql.impl.explain.QueryExplain;
import com.hazelcast.sql.impl.optimizer.SqlPlan;
import com.hazelcast.sql.impl.optimizer.SqlPlanType;

public class JetPlan implements SqlPlan {

    private final DAG dag;
    private final boolean isStreaming;
    private final boolean isInsert;
    private final QueryId queryId;
    private final SqlRowMetadata rowMetadata;

    public JetPlan(DAG dag, boolean isStreaming, boolean isInsert, QueryId queryId, SqlRowMetadata rowMetadata) {
        this.dag = dag;
        this.isStreaming = isStreaming;
        this.isInsert = isInsert;
        this.queryId = queryId;
        this.rowMetadata = rowMetadata;
    }

    @Override
    public SqlPlanType getType() {
        return SqlPlanType.JET;
    }

    @Override
    public QueryExplain getExplain() {
        throw new UnsupportedOperationException("TODO");
    }

    public DAG getDag() {
        return dag;
    }

    public boolean isStreaming() {
        return isStreaming;
    }

    public boolean isInsert() {
        return isInsert;
    }

    public QueryId getQueryId() {
        return queryId;
    }

    public SqlRowMetadata getRowMetadata() {
        return rowMetadata;
    }
}
