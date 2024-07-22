/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.sedona.core.spatialPartitioning;

import org.apache.sedona.core.spatialPartitioning.octtree.OctBox;
import org.apache.sedona.core.spatialPartitioning.octtree.StandardOctTree;
import org.locationtech.jts.geom.Envelope;

import java.io.Serializable;
import java.util.List;

public class OcttreePartitioning
        implements Serializable
{

    /**
     * The Quad-Tree.
     */
    private final StandardOctTree<Integer> partitionTree;

    /**
     * Instantiates a new Quad-Tree partitioning.
     *
     * @param samples the sample list
     * @param boundary the boundary
     * @param partitions the partitions
     */
    public OcttreePartitioning(List<Envelope> samples, Envelope boundary, int partitions)
            throws Exception
    {
        this(samples, boundary, partitions, -1);
    }

    public OcttreePartitioning(List<Envelope> samples, Envelope boundary, final int partitions, int minTreeLevel)
            throws Exception
    {
        // Make sure the tree doesn't get too deep in case of data skew
        int maxLevel = partitions;
        int maxItemsPerNode = samples.size() / partitions;
        partitionTree = new StandardOctTree(new OctBox(boundary), 0,
                maxItemsPerNode, maxLevel);
        if (minTreeLevel > 0) {
            partitionTree.forceGrowUp(minTreeLevel);
        }

        for (final Envelope sample : samples) {
            partitionTree.insert(new OctBox(sample), 1);
        }

        partitionTree.assignPartitionIds();
    }

    public StandardOctTree getPartitionTree()
    {
        return this.partitionTree;
    }
}
