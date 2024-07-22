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

import org.locationtech.jts.geom.Envelope;

import java.io.Serializable;

public class OctBox
        implements Serializable
{
    public final double x, y, z, width, height, depth;
    public Integer partitionId = null;
    public String lineage = null;

    public OctBox(Envelope envelope)
    {
        this.x = envelope.getMinX();
        this.y = envelope.getMinY();
        this.z = envelope.getMinZ();
        this.width = envelope.getWidth();
        this.height = envelope.getHeight();
        this.depth = envelope.getDepth();
    }

    public OctBox(double x, double y, double z, double width, double height, double depth)
    {
        if (width < 0) {
            throw new IllegalArgumentException("width must be >= 0");
        }

        if (height < 0) {
            throw new IllegalArgumentException("height must be >= 0");
        }

        if (depth < 0) {
            throw new IllegalArgumentException("depth must be >= 0");
        }


        this.x = x;
        this.y = y;
        this.z = z;
        this.width = width;
        this.height = height;
        this.depth = depth;
    }

    public boolean contains(double x, double y, double z)
    {
        return x >= this.x && x <= this.x + this.width
                && y >= this.y && y <= this.y + this.height
                    && z >= this.z && z <= this.z + this.depth;
    }

    public boolean contains(OctBox b)
    {
        return b.x >= this.x && b.x + b.width <= this.x + this.width
                && b.y >= this.y && b.y + b.height <= this.y + this.height
                    && b.z >= this.z && b.z + b.depth <= this.z + this.depth;
    }
    /*
    public boolean contains(int x, int y) {
        return this.width > 0 && this.height > 0
                && x >= this.x && x <= this.x + this.width
                && y >= this.y && y <= this.y + this.height;
    }
    */

    public int getUniqueId()
    {
        /*
        Long uniqueId = Long.valueOf(-1);
        try {
            uniqueId = Long.valueOf(RasterizationUtils.Encode2DTo1DId(resolutionX,resolutionY,(int)this.x,(int)this.y));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return uniqueId;
        */
        return hashCode();
    }

    public Envelope getEnvelope()
    {
        return new Envelope(x, x + width, y, y + height, z, z + depth);
    }

    @Override
    public String toString()
    {
        return "x: " + x + " y: " + y + " z: " + z +" w: " + width + " h: " + height + " d: "+ depth+  " PartitionId: " + partitionId + " Lineage: " + lineage;
    }

    @Override
    public boolean equals(Object o)
    {
        if (o == null || !(o instanceof OctBox)) {
            return false;
        }

        final OctBox other = (OctBox) o;
        return this.x == other.x && this.y == other.y && this.z == other.z
                && this.width == other.width && this.height == other.height && this.depth == other.depth
                && this.partitionId == other.partitionId;
    }

    @Override
    public int hashCode()
    {
        String stringId = "" + x + y + z + width + height + depth;
        return stringId.hashCode();
    }
}
