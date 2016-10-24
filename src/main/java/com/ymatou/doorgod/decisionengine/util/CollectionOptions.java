/*
 *
 *  (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *  All rights reserved.
 *
 */

package com.ymatou.doorgod.decisionengine.util;

/**
 * @author luoshiqian 2016/10/24 17:15
 */
public class CollectionOptions {
    private Long maxDocuments;

    private Long size;

    private Boolean capped;

    /**
     * Constructs a new <code>CollectionOptions</code> instance.
     *
     * @param size the collection size in bytes, this data space is preallocated
     * @param maxDocuments the maximum number of documents in the collection.
     * @param capped true to created a "capped" collection (fixed size with auto-FIFO behavior based on insertion order),
     *          false otherwise.
     */
    public CollectionOptions(Long size, Long maxDocuments, Boolean capped) {
        super();
        this.maxDocuments = maxDocuments;
        this.size = size;
        this.capped = capped;
    }

    public Long getMaxDocuments() {
        return maxDocuments;
    }

    public void setMaxDocuments(Long maxDocuments) {
        this.maxDocuments = maxDocuments;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public Boolean getCapped() {
        return capped;
    }

    public void setCapped(Boolean capped) {
        this.capped = capped;
    }
}
