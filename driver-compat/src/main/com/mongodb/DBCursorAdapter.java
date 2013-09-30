/*
 * Copyright (c) 2008 - 2013 10gen, Inc. <http://10gen.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mongodb;


import org.mongodb.ServerCursor;


class DBCursorAdapter implements MongoCursor {
    DBCursor cursor;

    public DBCursorAdapter(final DBCursor cursor) {
        this.cursor = cursor;
    }

    public ServerCursor getServerCursor() {
        ServerAddress serverAddress = cursor.getServerAddress();
        return new ServerCursor(cursor.getCursorId(), 
            new org.mongodb.connection.ServerAddress(serverAddress.getHost(), serverAddress.getPort()));
    }

    @Override
    public void close() {
        cursor.close();
    }

    @Override
    public boolean hasNext() {
        return cursor.hasNext();
    }

    @Override
    public DBObject next() {
        return cursor.next();
    }

    @Override
    public void remove() {
        cursor.remove();
    }
}