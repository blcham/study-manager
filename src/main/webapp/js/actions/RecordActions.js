import * as ActionConstants from "../constants/ActionConstants";
import {ACTION_FLAG, ACTION_STATUS} from "../constants/DefaultConstants";
import {axiosBackend} from "./index";
import * as Utils from "../utils/Utils";
import {loadRecords} from "./RecordsActions";
import {API_URL} from '../../config';

export function deleteRecord(record, currentUser) {
    //console.log("Deleting record: ", record);
    return function (dispatch) {
        dispatch(deleteRecordPending(record.key));
        axiosBackend.delete(`${API_URL}/rest/records/${record.key}`, {
            ...record
        }).then(() => {
            dispatch(loadRecords(currentUser));
            dispatch(deleteRecordSuccess(record, record.key));
        }).catch((error) => {
            dispatch(deleteRecordError(error.response.data, record, record.key));
        });
    }
}

export function deleteRecordPending(key) {
    return {
        type: ActionConstants.DELETE_RECORD_PENDING,
        key
    }
}

export function deleteRecordSuccess(record, key) {
    return {
        type: ActionConstants.DELETE_RECORD_SUCCESS,
        record,
        key
    }
}

export function deleteRecordError(error, record, key) {
    return {
        type: ActionConstants.DELETE_RECORD_ERROR,
        error,
        record,
        key
    }
}

export function loadRecord(key) {
    //console.log("Loading record with key: ", key);
    return function (dispatch) {
        dispatch(loadRecordPending());
        axiosBackend.get(`${API_URL}/rest/records/${key}`).then((response) => {
            dispatch(loadRecordSuccess(response.data));
        }).catch((error) => {
            dispatch(loadRecordError(error.response.data));
        });
    }
}

export function loadRecordPending() {
    return {
        type: ActionConstants.LOAD_RECORD_PENDING
    }
}

export function loadRecordSuccess(record) {
    return {
        type: ActionConstants.LOAD_RECORD_SUCCESS,
        record
    }
}

export function loadRecordError(error) {
    return {
        type: ActionConstants.LOAD_RECORD_ERROR,
        error
    }
}

export function unloadRecord() {
    return {
        type: ActionConstants.UNLOAD_RECORD
    }
}

export function createRecord(record, currentUser) {
    //console.log("Creating record: ", record);
    return function (dispatch) {
        dispatch(saveRecordPending(ACTION_FLAG.CREATE_ENTITY));
        axiosBackend.post(`${API_URL}/rest/records`, {
            ...record
        }).then((response) => {
            const key = Utils.extractKeyFromLocationHeader(response);
            dispatch(saveRecordSuccess(record, key, ACTION_FLAG.CREATE_ENTITY));
            dispatch(loadRecords(currentUser));
        }).catch((error) => {
            dispatch(saveRecordError(error.response.data, record, ACTION_FLAG.CREATE_ENTITY));
        });
    }
}

export function updateRecord(record, currentUser) {
    //console.log("Updating record: ", record);
    return function (dispatch) {
        dispatch(saveRecordPending(ACTION_FLAG.UPDATE_ENTITY));
        axiosBackend.put(`${API_URL}/rest/records/${record.key}`, {
            ...record
        }).then((response) => {
            dispatch(saveRecordSuccess(record, null, ACTION_FLAG.UPDATE_ENTITY));
            dispatch(loadRecords(currentUser));
        }).catch((error) => {
            dispatch(saveRecordError(error.response.data, record, ACTION_FLAG.UPDATE_ENTITY));
        });
    }
}

export function saveRecordPending(actionFlag) {
    return {
        type: ActionConstants.SAVE_RECORD_PENDING,
        actionFlag
    }
}

export function saveRecordSuccess(record, key, actionFlag) {
    return {
        type: ActionConstants.SAVE_RECORD_SUCCESS,
        record,
        key,
        actionFlag
    }
}

export function saveRecordError(error, record, actionFlag) {
    return {
        type: ActionConstants.SAVE_RECORD_ERROR,
        error,
        record,
        actionFlag
    }
}

export function unloadSavedRecord() {
    return {
        type: ActionConstants.UNLOAD_SAVED_RECORD
    }
}

export function loadFormgen(status, error = null) {
    switch (status) {
        case ACTION_STATUS.PENDING:
            return {
                type: ActionConstants.LOAD_FORMGEN_PENDING
            };
        case ACTION_STATUS.SUCCESS:
            return {
                type: ActionConstants.LOAD_FORMGEN_SUCCESS
            };
        case ACTION_STATUS.ERROR:
            return {
                type: ActionConstants.LOAD_FORMGEN_ERROR,
                error
            }
    }
}