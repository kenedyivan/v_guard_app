package com.project.ken.vecurityguard.Models;

import java.util.List;

/**
 * Created by ken on 3/6/18.
 */

public class FCMResponse {
    public long multicate_id;
    public int success;
    public int failure;
    public int canonical_ids;
    public List<Result> result;

    public FCMResponse() {
    }

    public FCMResponse(long multicate_id, int success, int failure, int canonical_ids, List<Result> result) {
        this.multicate_id = multicate_id;
        this.success = success;
        this.failure = failure;
        this.canonical_ids = canonical_ids;
        this.result = result;
    }

    public long getMulticate_id() {
        return multicate_id;
    }

    public void setMulticate_id(long multicate_id) {
        this.multicate_id = multicate_id;
    }

    public int getSuccess() {
        return success;
    }

    public void setSuccess(int success) {
        this.success = success;
    }

    public int getFailure() {
        return failure;
    }

    public void setFailure(int failure) {
        this.failure = failure;
    }

    public int getCanonical_ids() {
        return canonical_ids;
    }

    public void setCanonical_ids(int canonical_ids) {
        this.canonical_ids = canonical_ids;
    }

    public List<Result> getResult() {
        return result;
    }

    public void setResult(List<Result> result) {
        this.result = result;
    }
}
