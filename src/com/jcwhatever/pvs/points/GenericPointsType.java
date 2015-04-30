/*
 * This file is part of PV-Star for Bukkit, licensed under the MIT License (MIT).
 *
 * Copyright (c) JCThePants (www.jcwhatever.com)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.jcwhatever.pvs.points;

import com.jcwhatever.nucleus.storage.IDataNode;
import com.jcwhatever.nucleus.utils.PreCon;
import com.jcwhatever.pvs.api.arena.IArena;
import com.jcwhatever.pvs.api.points.IPointsHandler;
import com.jcwhatever.pvs.api.points.PointsType;

/**
 * Generic points type.
 */
public class GenericPointsType extends PointsType {

    @Override
    public String getName() {
        return "Points";
    }

    @Override
    public String getDescription() {
        return "Generic points type.";
    }

    @Override
    protected IPointsHandler getNewHandler(IArena arena, IDataNode dataNode) {
        return new GenericPointsHandler(arena, this, dataNode);
    }

    @Override
    protected void onRemove(IArena arena, IPointsHandler handler) {
        // do nothing
    }

    public static class GenericPointsHandler implements IPointsHandler {

        private final IArena _arena;
        private final PointsType _type;
        private final IDataNode _dataNode;
        private int _points = 1;

        public GenericPointsHandler(IArena arena, PointsType type, IDataNode node) {
            PreCon.notNull(arena);
            PreCon.notNull(type);
            PreCon.notNull(node);

            _arena = arena;
            _type = type;
            _dataNode = node;
            _points = _dataNode.getInteger("points", _points);
        }

        @Override
        public IArena getArena() {
            return _arena;
        }

        @Override
        public PointsType getPointsType() {
            return _type;
        }

        @Override
        public int getPoints() {
            return _points;
        }

        @Override
        public void setPoints(int points) {
            _points = points;

            _dataNode.set("points", points);
            _dataNode.save();
        }
    }
}
