import {axiosBackend} from "./index";
import {Routes} from "../utils/Routes";
import {transitionTo, transitionToHome} from "../utils/Routing";
import * as ActionConstants from "../constants/ActionConstants";

export function login(username, password) {
    return function (dispatch) {
        axiosBackend.post('j_spring_security_check', `username=${username}&password=${password}`,
            {headers: {'Content-Type': 'application/x-www-form-urlencoded'}
            }).then((response) => {
            const data = response.data;
            if (!data.success || !data.loggedIn) {
                dispatch(userAuthError(response.data));
                return;
            }
            dispatch(userAuthSuccess());
            dispatch(loadUserProfile());
            transitionToHome();
        }).catch((error) => {
            dispatch(userAuthError(error.response.data));
        });
    }
}

export function userAuthSuccess() {
    return {
        type: ActionConstants.AUTH_USER
    }
}

export function userAuthError(error) {
    return {
        type: ActionConstants.AUTH_USER_ERROR,
        error
    }
}

export function logout() {
    //console.log("Logouting user");
    return function (dispatch) {
        axiosBackend.post('j_spring_security_logout').then(() => {
            dispatch(unauthUser());
            //Logger.log('User successfully logged out.');
            transitionTo(Routes.login);
        }).catch((error) => {
            /* TODO maybe action error */
            //Logger.error('Logout failed. Status: ' + error.status);
        });
    }
}

export function unauthUser() {
    return {
        type: ActionConstants.UNAUTH_USER
    }
}

export function loadUserProfile() {
    //console.log("Loading user profile");
    return function (dispatch) {
        dispatch(loadUserProfilePending());
        axiosBackend.get('rest/users/current').then((response) => {
            dispatch(loadUserProfileSuccess(response.data));
        }).catch ((error) => {
            dispatch(loadUserProfileError(error.response.data));
        });
    }
}

export function loadUserProfilePending() {
    return {
        type: ActionConstants.LOAD_USER_PROFILE_PENDING
    }
}

export function loadUserProfileSuccess(user) {
    return {
        type: ActionConstants.LOAD_USER_PROFILE_SUCCESS,
        user
    }
}

export function loadUserProfileError(error) {
    return {
        type: ActionConstants.LOAD_USER_PROFILE_ERROR,
        error
    }
}