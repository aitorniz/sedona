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

package org.apache.sedona.core.spatialPartitioning.octtree;


import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;

import org.apache.sedona.core.spatialPartitioning.octtree.OctNode;
import org.apache.sedona.core.spatialPartitioning.Visitor;

public class StandardOctTree<T> 
	implements Serializable{
    public static final int REGION_SELF = -1;
    // U for upper and D for down
    // North, East, West and South for the rest
    public static final int REGION_NWD = 0;
    public static final int REGION_NED = 1;
    public static final int REGION_SWD = 2;
    public static final int REGION_SED = 3;
    public static final int REGION_NWU = 4;
    public static final int REGION_NEU = 5;
    public static final int REGION_SWU = 6;
    public static final int REGION_SEU = 7;
    // Maximum number of items in any given zone. When reached, a zone is sub-divided.
    private final int maxItemsPerZone;
    private final int maxLevel;
    private final int level;
    // the current nodes
    private final List<OctNode<T>> nodes = new ArrayList<>();
    // current box (as we are in 3D) zone
    private final OctBox zone;
    //DO we need to change the following nodeNum for the 3D ?
    private int nodeNum = 0;
    // the four sub regions,
    private StandardOctTree<T>[] regions;

    public StandardOctTree(OctBox definition, int level)
    {
        this(definition, level, 5, 10);
    }
    public StandardOctTree(OctBox definition, int level, int maxItemsPerZone, int maxLevel)
    {
        this.maxItemsPerZone = maxItemsPerZone;
        this.maxLevel = maxLevel;
        this.zone = definition;
        this.level = level;
    }

    public OctBox getZone()
    {
        return this.zone;
    }
    private int findRegion(OctBox r, boolean split)
    {
        int region = REGION_SELF;
        if (nodeNum >= maxItemsPerZone && this.level < maxLevel) {
            // we don't want to split if we just need to retrieve
            // the region, not inserting an element
            if (regions == null && split) {
                // then create the subregions
                this.split();
            }

            // can be null if not split
            if (regions != null) {
                for (int i = 0; i < regions.length; i++) {
                    if (regions[i].getZone().contains(r)) {
                        region = i;
                        break;
                    }
                }
            }
        }
	return region;
     }
     private int findRegion(int x, int y, int z)
    {
        int region = REGION_SELF;
        // can be null if not split
	// knowing that regions is of StandardOctTree<T>[] type
        if (regions != null) {
            for (int i = 0; i < regions.length; i++) {
                if (regions[i].getZone().contains(x, y, z)) {
                    region = i;
                    break;
                }
            }
        }
        return region;
    }
    private StandardOctTree<T> newOctTree(OctBox zone, int level)
    {
        return new StandardOctTree<T>(zone, level, this.maxItemsPerZone, this.maxLevel);
    }
    private void split()
    {

        regions = new StandardOctTree[4];

        double newWidth = zone.width / 2;
        double newHeight = zone.height / 2;
	//as we need a third dimension
	//we need the depth
    	double newDepth = zone.depth / 2;
        int newLevel = level + 1;

        regions[REGION_NWD] = newOctTree(new OctBox(
                zone.x,
                zone.y + zone.height / 2,
		        zone.z,
                newWidth,
                newHeight,
		        newDepth
                ), newLevel);

        regions[REGION_NED] = newOctTree(new OctBox(
                zone.x + zone.width / 2,
                zone.y + zone.height / 2,
		zone.z,
                newWidth,
                newHeight,
		newDepth
        ), newLevel);

        regions[REGION_SWD] = newOctTree(new OctBox(
                zone.x,
		zone.y,
		zone.z,
                newWidth,
                newHeight,
		newDepth
        ), newLevel);

        regions[REGION_SED] = newOctTree(new OctBox(
                zone.x + zone.width / 2,
                zone.y,
         		zone.z,
                newWidth,
                newHeight,
		        newDepth
        ), newLevel);

        regions[REGION_NWU] = newOctTree(new OctBox(
                zone.x,
                zone.y + zone.height / 2,
		zone.z + zone.depth / 2,
                newWidth,
                newHeight,
		newDepth
        ), newLevel);

        regions[REGION_NEU] = newOctTree(new OctBox(
                zone.x + zone.width / 2,
                zone.y + zone.height / 2,
		zone.z + zone.depth / 2,
                newWidth,
                newHeight,
		        newDepth
        ), newLevel);
	regions[REGION_SWD] = newOctTree(new OctBox(
                zone.x,
                zone.y,
                zone.z + zone.depth / 2,
                newWidth,
                newHeight,
                newDepth
        ), newLevel);

        regions[REGION_SED] = newOctTree(new OctBox(
                zone.x + zone.width / 2,
                zone.y,
                zone.z + zone.depth / 2,
                newWidth,
                newHeight,
                newDepth
        ), newLevel);
    }
	// Force the quad tree to grow up to a certain level.
    public void forceGrowUp(int minLevel)
    {
        if (minLevel < 1) {
            throw new IllegalArgumentException("minLevel must be >= 1. Received " + minLevel);
        }

        split();
        nodeNum = maxItemsPerZone;
        if (level + 1 >= minLevel) {

            return;
        }

        for (StandardOctTree<T> region : regions) {
            region.forceGrowUp(minLevel);
        }
    }

    public void insert(OctBox b, T element)
    {
        int region = this.findRegion(b, true);
        if (region == REGION_SELF || this.level == maxLevel) {
            nodes.add(new OctNode<T>(b, element));
            nodeNum++;
            return;
        }
        else {
            regions[region].insert(b, element);
        }

        if (nodeNum >= maxItemsPerZone && this.level < maxLevel) {
            // redispatch the elements
            List<OctNode<T>> tempNodes = new ArrayList<>();
            tempNodes.addAll(nodes);

            nodes.clear();
            for (OctNode<T> node : tempNodes) {
                this.insert(node.b, node.element);
            }
        }
    }
    public void dropElements()
    {
        traverse(new Visitor<T>()
        {
            @Override
            public boolean visit(StandardOctTree<T> tree)
            {
                tree.nodes.clear();
                return true;
            }
        });
    }
    public List<T> getElements(OctBox b)
    {
        int region = this.findRegion(b, false);

        final List<T> list = new ArrayList<>();

        if (region != REGION_SELF) {
            for (OctNode<T> node : nodes) {
                list.add(node.element);
            }

            list.addAll(regions[region].getElements(b));
        }
        else {
            addAllElements(list);
        }

        return list;
    }
    private void traverse(Visitor<T> visitor)
    {
        if (!visitor.visit(this)) {
            return;
        }

        if (regions != null) {
            regions[REGION_NWD].traverse(visitor);
            regions[REGION_NED].traverse(visitor);
            regions[REGION_SWD].traverse(visitor);
            regions[REGION_SED].traverse(visitor);
	        regions[REGION_NWU].traverse(visitor);
            regions[REGION_NEU].traverse(visitor);
            regions[REGION_SWU].traverse(visitor);
            regions[REGION_SEU].traverse(visitor);
        }
    }
    /**
     * Traverses the tree top-down breadth-first and calls the visitor
     * for each node. Stops traversing if a call to Visitor.visit returns false.
     * lineage will memorize the traversal path for each nodes
     */
    private void traverseWithTrace(VisitorWithLineage<T> visitor, String lineage)
    {
        if (!visitor.visit(this, lineage)) {
            return;
        }

        if (regions != null) {
            regions[REGION_NWD].traverseWithTrace(visitor, lineage + REGION_NWD);
            regions[REGION_NED].traverseWithTrace(visitor, lineage + REGION_NED);
            regions[REGION_SWD].traverseWithTrace(visitor, lineage + REGION_SWD);
            regions[REGION_SED].traverseWithTrace(visitor, lineage + REGION_SED);
	    regions[REGION_NWU].traverseWithTrace(visitor, lineage + REGION_NWU);
            regions[REGION_NEU].traverseWithTrace(visitor, lineage + REGION_NEU);
            regions[REGION_SWU].traverseWithTrace(visitor, lineage + REGION_SWU);
            regions[REGION_SEU].traverseWithTrace(visitor, lineage + REGION_SEU);
        }
    }
    private void addAllElements(final List<T> list)
    {
        traverse(new Visitor<T>()
        {
            @Override
            public boolean visit(StandardOctTree<T> tree)
            {
                for (OctNode<T> node : tree.nodes) {
                    list.add(node.element);
                }
                return true;
            }
        });
    }
}
