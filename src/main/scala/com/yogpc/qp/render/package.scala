package com.yogpc.qp

import net.minecraft.client.renderer.BufferBuilder
import net.minecraft.util.math.{Vec3d, Vec3i}

package object render {

    implicit class BufferBuilderHelper(val buffer: BufferBuilder) extends AnyVal {
        def pos(vec: Vec3d): BufferBuilder = buffer.pos(vec.x, vec.y, vec.z)

        /**
          * buffer.color(255, 255, 255, 255)
          *
          * @return the buffer
          */
        def colored(): BufferBuilder = buffer.color(255, 255, 255, 255)

        /**
          * buffer.lightmap(240, 0).endVertex()
          */
        def lightedAndEnd(): Unit = buffer.lightmap(240, 0).endVertex()
    }

    implicit class Vec3dHelper(val vec3d: Vec3d) extends AnyVal {
        def +(o: Vec3d): Vec3d = vec3d add o

        def +(o: Vec3i): Vec3d = vec3d addVector(o.getX, o.getY, o.getZ)

        def -(o: Vec3d): Vec3d = vec3d subtract o

        def -(o: Vec3i): Vec3d = vec3d subtract(o.getX, o.getY, o.getZ)
    }

}
