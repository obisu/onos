/*
 * Copyright 2016-present Open Networking Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.onosproject.store.primitives.resources.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Maps;
import io.atomix.protocols.raft.operation.OperationId;
import io.atomix.protocols.raft.operation.OperationType;
import org.onlab.util.KryoNamespace;
import org.onlab.util.Match;
import org.onosproject.store.serializers.KryoNamespaces;
import org.onosproject.store.service.Versioned;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * AsyncConsistentMultimap state machine commands.
 */
public enum AtomixConsistentSetMultimapOperations implements OperationId {
    GET(OperationType.QUERY),
    SIZE(OperationType.QUERY),
    IS_EMPTY(OperationType.QUERY),
    CONTAINS_KEY(OperationType.QUERY),
    CONTAINS_VALUE(OperationType.QUERY),
    CONTAINS_ENTRY(OperationType.QUERY),
    KEY_SET(OperationType.QUERY),
    KEYS(OperationType.QUERY),
    VALUES(OperationType.QUERY),
    ENTRIES(OperationType.QUERY),
    PUT(OperationType.COMMAND),
    PUT_AND_GET(OperationType.COMMAND),
    REMOVE(OperationType.COMMAND),
    REMOVE_AND_GET(OperationType.COMMAND),
    REMOVE_ALL(OperationType.COMMAND),
    REPLACE(OperationType.COMMAND),
    CLEAR(OperationType.COMMAND),
    ADD_LISTENER(OperationType.COMMAND),
    REMOVE_LISTENER(OperationType.COMMAND),
    OPEN_ITERATOR(OperationType.COMMAND),
    NEXT(OperationType.QUERY),
    CLOSE_ITERATOR(OperationType.COMMAND);

    private final OperationType type;

    AtomixConsistentSetMultimapOperations(OperationType type) {
        this.type = type;
    }

    @Override
    public String id() {
        return name();
    }

    @Override
    public OperationType type() {
        return type;
    }

    public static final KryoNamespace NAMESPACE = KryoNamespace.newBuilder()
            .register(KryoNamespaces.BASIC)
            .nextId(KryoNamespaces.BEGIN_USER_CUSTOM_ID)
            .register(ContainsEntry.class)
            .register(ContainsKey.class)
            .register(ContainsValue.class)
            .register(Get.class)
            .register(MultiRemove.class)
            .register(Put.class)
            .register(RemoveAll.class)
            .register(Replace.class)
            .register(Match.class)
            .register(Versioned.class)
            .register(ArrayList.class)
            .register(Maps.immutableEntry("", "").getClass())
            .register(IteratorBatch.class)
            .register(IteratorPosition.class)
            .build("AtomixConsistentSetMultimapOperations");

    /**
     * Abstract multimap command.
     */
    @SuppressWarnings("serial")
    public abstract static class MultimapOperation {
        @Override
        public String toString() {
            return MoreObjects.toStringHelper(getClass())
                    .toString();
        }
    }

    /**
     * Abstract key-based multimap query.
     */
    @SuppressWarnings("serial")
    public abstract static class KeyOperation extends MultimapOperation {
        protected String key;

        public KeyOperation() {
        }

        public KeyOperation(String key) {
            this.key = checkNotNull(key);
        }

        public String key() {
            return key;
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(getClass())
                    .add("key", key)
                    .toString();
        }
    }

    /**
     * Abstract value-based query.
     */
    @SuppressWarnings("serial")
    public abstract static class ValueOperation extends MultimapOperation {
        protected byte[] value;

        public ValueOperation() {
        }

        public ValueOperation(byte[] value) {
            this.value = checkNotNull(value);
        }

        /**
         * Returns the value.
         *
         * @return value.
         */
        public byte[] value() {
            return value;
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(getClass())
                    .add("value", value)
                    .toString();
        }
    }

    /**
     * Contains key query.
     */
    @SuppressWarnings("serial")
    public static class ContainsKey extends KeyOperation {
        public ContainsKey() {
        }

        public ContainsKey(String key) {
            super(key);
        }
    }

    /**
     * Contains value query.
     */
    @SuppressWarnings("serial")
    public static class ContainsValue extends ValueOperation {
        public ContainsValue() {
        }

        public ContainsValue(byte[] value) {
            super(value);
        }
    }

    /**
     * Contains entry query.
     */
    @SuppressWarnings("serial")
    public static class ContainsEntry extends MultimapOperation {
        protected String key;
        protected byte[] value;

        public ContainsEntry() {
        }

        public ContainsEntry(String key, byte[] value) {
            this.key = checkNotNull(key);
            this.value = checkNotNull(value);
        }

        public String key() {
            return key;
        }

        public byte[] value() {
            return value;
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(getClass())
                    .add("key", key)
                    .add("value", value)
                    .toString();
        }
    }

    /**
     * Remove command, backs remove and removeAll's that return booleans.
     */
    @SuppressWarnings("serial")
    public static class RemoveAll extends MultimapOperation {
        private String key;
        private Match<Long> versionMatch;

        public RemoveAll() {
        }

        public RemoveAll(String key, Match<Long> versionMatch) {
            this.key = checkNotNull(key);
            this.versionMatch = versionMatch;
        }

        public String key() {
            return this.key;
        }

        public Match<Long> versionMatch() {
            return versionMatch;
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(getClass())
                    .add("key", key)
                    .add("versionMatch", versionMatch)
                    .toString();
        }
    }

    /**
     * Remove command, backs remove and removeAll's that return booleans.
     */
    @SuppressWarnings("serial")
    public static class MultiRemove extends MultimapOperation {
        private String key;
        private Collection<byte[]> values;
        private Match<Long> versionMatch;

        public MultiRemove() {
        }

        public MultiRemove(String key, Collection<byte[]> valueMatches,
                           Match<Long> versionMatch) {
            this.key = checkNotNull(key);
            this.values = valueMatches;
            this.versionMatch = versionMatch;
        }

        public String key() {
            return this.key;
        }

        public Collection<byte[]> values() {
            return values;
        }

        public Match<Long> versionMatch() {
            return versionMatch;
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(getClass())
                    .add("key", key)
                    .add("values", values)
                    .add("versionMatch", versionMatch)
                    .toString();
        }
    }

    /**
     * Command to back the put and putAll methods.
     */
    @SuppressWarnings("serial")
    public static class  Put extends MultimapOperation {
        private String key;
        private Collection<? extends byte[]> values;
        private Match<Long> versionMatch;

        public Put() {
        }

        public Put(String key, Collection<? extends byte[]> values, Match<Long> versionMatch) {
            this.key = checkNotNull(key);
            this.values = values;
            this.versionMatch = versionMatch;
        }

        public String key() {
            return key;
        }

        public Collection<? extends byte[]> values() {
            return values;
        }

        public Match<Long> versionMatch() {
            return versionMatch;
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(getClass())
                    .add("key", key)
                    .add("values", values)
                    .add("versionMatch", versionMatch)
                    .toString();
        }
    }

    /**
     * Replace command, returns the collection that was replaced.
     */
    @SuppressWarnings("serial")
    public static class Replace extends MultimapOperation {
        private String key;
        private Collection<byte[]> values;
        private Match<Long> versionMatch;

        public Replace() {
        }

        public Replace(String key, Collection<byte[]> values,
                       Match<Long> versionMatch) {
            this.key = checkNotNull(key);
            this.values = values;
            this.versionMatch = versionMatch;
        }

        public String key() {
            return this.key;
        }

        public Match<Long> versionMatch() {
            return versionMatch;
        }

        public Collection<byte[]> values() {
            return values;
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(getClass())
                    .add("key", key)
                    .add("values", values)
                    .add("versionMatch", versionMatch)
                    .toString();
        }
    }

    /**
     * Get value query.
     */
    public static class Get extends KeyOperation {
        public Get() {
        }

        public Get(String key) {
            super(key);
        }
    }

    /**
     * Iterator position.
     */
    public static class IteratorPosition {
        private long iteratorId;
        private int position;

        private IteratorPosition() {
        }

        public IteratorPosition(long iteratorId, int position) {
            this.iteratorId = iteratorId;
            this.position = position;
        }

        public long iteratorId() {
            return iteratorId;
        }

        public int position() {
            return position;
        }
    }

    /**
     * Iterator batch.
     */
    public static class IteratorBatch implements Iterator<Map.Entry<String, byte[]>> {
        private int position;
        private Collection<Map.Entry<String, byte[]>> entries;
        private transient volatile Iterator<Map.Entry<String, byte[]>> iterator;

        private IteratorBatch() {
        }

        public IteratorBatch(int position, Collection<Map.Entry<String, byte[]>> entries) {
            this.position = position;
            this.entries = entries;
        }

        public int position() {
            return position;
        }

        public Collection<Map.Entry<String, byte[]>> entries() {
            return entries;
        }

        private Iterator<Map.Entry<String, byte[]>> iterator() {
            Iterator<Map.Entry<String, byte[]>> iterator = this.iterator;
            if (iterator == null) {
                synchronized (entries) {
                    iterator = this.iterator;
                    if (iterator == null) {
                        iterator = entries.iterator();
                        this.iterator = iterator;
                    }
                }
            }
            return iterator;
        }

        @Override
        public boolean hasNext() {
            return iterator().hasNext();
        }

        @Override
        public Map.Entry<String, byte[]> next() {
            return iterator().next();
        }
    }
}
