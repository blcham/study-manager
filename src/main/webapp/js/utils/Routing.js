'use strict';

import {execute} from "./RoutingRules";
import {setTransitionPayload} from "../actions/RouterActions";
import {createBrowserHistory} from 'history';
import {setViewHandlers} from "../actions/RouterActions";
import * as Constants from "../constants/DefaultConstants";
import {BASENAME} from "../../config";

export const history = createBrowserHistory({
    basename: BASENAME
});

/**
 * Transitions to the specified route
 * @param route Route object
 */
export function transitionTo(route) {
    let path = route.path;
    execute(route.name);
    history.push({
        pathname: path,
    });
}

/**
 * Transitions to the specified route
 * @param route Route object
 * @param options Transition options, can specify path parameters, query parameters, payload and view handlers.
 */
export function transitionToWithOpts(route, options) {
    return function (dispatch) {
        let path = route.path;
        if (!options) {
            options = {};
        }
        if (options.params) {
            path = setPathParams(path, options.params);
        }
        if (options.payload) {
            dispatch(setTransitionPayload(route.name, options.payload));
        }
        if (options.handlers) {
            dispatch(setViewHandlers(route.name, options.handlers));
        }
        execute(route.name);
        history.push({
            pathname: path,
            search: options.query
        });
    }
}

// TODO
function setPathParams(path, params) {
    for (let paramName in params) {
        if (params.hasOwnProperty(paramName)) {
            path = path.replace(':' + paramName, params[paramName]);
        }
    }
    return path;
}

export function transitionToHome() {
    transitionTo(Constants.HOME_ROUTE);
}

