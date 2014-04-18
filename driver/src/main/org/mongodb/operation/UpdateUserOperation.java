/*
 * Copyright (c) 2008-2014 MongoDB, Inc.
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

package org.mongodb.operation;

import org.mongodb.CommandResult;
import org.mongodb.Document;
import org.mongodb.MongoFuture;
import org.mongodb.MongoNamespace;
import org.mongodb.WriteConcern;
import org.mongodb.WriteResult;
import org.mongodb.binding.AsyncWriteBinding;
import org.mongodb.binding.WriteBinding;
import org.mongodb.codecs.DocumentCodec;
import org.mongodb.connection.Connection;
import org.mongodb.connection.ServerVersion;
import org.mongodb.protocol.ReplaceProtocol;

import static java.util.Arrays.asList;
import static org.mongodb.operation.OperationHelper.AsyncCallableWithConnection;
import static org.mongodb.operation.OperationHelper.VoidTransformer;
import static org.mongodb.operation.OperationHelper.executeProtocolAsync;
import static org.mongodb.operation.OperationHelper.executeWrappedCommandProtocol;
import static org.mongodb.operation.OperationHelper.executeWrappedCommandProtocolAsync;
import static org.mongodb.operation.OperationHelper.serverVersionIsAtLeast;
import static org.mongodb.operation.OperationHelper.withConnection;
import static org.mongodb.operation.UserOperationHelper.asCollectionDocument;
import static org.mongodb.operation.UserOperationHelper.asCollectionQueryDocument;
import static org.mongodb.operation.UserOperationHelper.asCommandDocument;

/**
 * An operation that updates a user.
 *
 * @since 3.0
 */
public class UpdateUserOperation implements AsyncWriteOperation<Void>, WriteOperation<Void>  {
    private final User user;

    public UpdateUserOperation(final User user) {
        this.user = user;
    }

    @Override
    public Void execute(final WriteBinding binding) {
        return withConnection(binding, new OperationHelper.CallableWithConnection<Void>() {
            @Override
            public Void call(final Connection connection) {
                if (serverVersionIsAtLeast(connection, new ServerVersion(2, 6))) {
                    executeWrappedCommandProtocol(user.getCredential().getSource(), getCommand(), connection);
                } else {
                    getCollectionBasedProtocol().execute(connection);
                }
                return null;
            }
        });
    }

    @Override
    public MongoFuture<Void> executeAsync(final AsyncWriteBinding binding) {
        return withConnection(binding, new AsyncCallableWithConnection<Void>() {
            @Override
            public MongoFuture<Void> call(final Connection connection) {
                if (serverVersionIsAtLeast(connection, new ServerVersion(2, 6))) {
                    return executeWrappedCommandProtocolAsync(user.getCredential().getSource(), getCommand(), connection,
                                                              new VoidTransformer<CommandResult>());
                } else {
                    return executeProtocolAsync(getCollectionBasedProtocol(), connection, new VoidTransformer<WriteResult>());
                }
            }
        });
    }

    @SuppressWarnings("unchecked")
    private ReplaceProtocol<Document> getCollectionBasedProtocol() {
        MongoNamespace namespace = new MongoNamespace(user.getCredential().getSource(), "system.users");
        DocumentCodec codec = new DocumentCodec();
        return new ReplaceProtocol<Document>(namespace, true, WriteConcern.ACKNOWLEDGED,
                asList(new ReplaceRequest<Document>(asCollectionQueryDocument(user), asCollectionDocument(user))),
                codec, codec);
    }

    private Document getCommand() {
        return asCommandDocument(user, "updateUser");
    }

}
