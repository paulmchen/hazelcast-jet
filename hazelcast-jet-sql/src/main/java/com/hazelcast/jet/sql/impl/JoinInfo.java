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

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;
import com.hazelcast.sql.impl.expression.Expression;

import java.io.IOException;

public class JoinInfo implements DataSerializable {

    private int[] leftEquiJoinIndices;
    private int[] rightEquiJoinIndices;
    private Expression<Boolean> nonEquiCondition;

    private Expression<Boolean> condition;

    @SuppressWarnings("unused")
    private JoinInfo() {
    }

    public JoinInfo(
            int[] leftEquiJoinIndices,
            int[] rightEquiJoinIndices,
            Expression<Boolean> nonEquiCondition,
            Expression<Boolean> condition
    ) {
        this.leftEquiJoinIndices = leftEquiJoinIndices;
        this.rightEquiJoinIndices = rightEquiJoinIndices;
        this.nonEquiCondition = nonEquiCondition;

        this.condition = condition;
    }

    public int[] leftEquiJoinIndices() {
        return leftEquiJoinIndices;
    }

    public int[] rightEquiJoinIndices() {
        return rightEquiJoinIndices;
    }

    public Expression<Boolean> nonEquiCondition() {
        return nonEquiCondition;
    }

    public Expression<Boolean> condition() {
        return condition;
    }

    public boolean isEquiJoin() {
        return rightEquiJoinIndices.length > 0;
    }

    @Override
    public void writeData(ObjectDataOutput out) throws IOException {
        out.writeObject(leftEquiJoinIndices);
        out.writeObject(rightEquiJoinIndices);
        out.writeObject(nonEquiCondition);
        out.writeObject(condition);
    }

    @Override
    public void readData(ObjectDataInput in) throws IOException {
        leftEquiJoinIndices = in.readObject();
        rightEquiJoinIndices = in.readObject();
        nonEquiCondition = in.readObject();
        condition = in.readObject();
    }
}