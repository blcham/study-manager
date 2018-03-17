'use strict';

import React from "react";
import {Alert, Button, Form, Panel} from "react-bootstrap";
import HorizontalInput from "../HorizontalInput";
import I18nWrapper from "../../i18n/I18nWrapper";
import injectIntl from "../../utils/injectIntl";
import Mask from "../Mask";
import * as Routing from "../../utils/Routing";
import * as Routes from "../../utils/Routes";
import {login} from "../../actions";
import {connect} from "react-redux";
import {bindActionCreators} from "redux";
import {ACTION_STATUS, ALERT_TYPES} from "../../constants/DefaultConstants";
import AlertMessage from "../AlertMessage";
import {transitionTo} from "../../utils/Routing";

class Login extends React.Component {
    constructor(props) {
        super(props);
        this.i18n = this.props.i18n;
        this.state = {
            username: '',
            password: '',
            showAlert: false
        }
    }

    componentDidMount() {
        this.usernameField.focus();
    }

    onChange = (e) => {
        let state = this.state;
        state[e.target.name] = e.target.value;
        state.alertVisible = false;
        this.setState(state);
    };

    onKeyPress = (e) => {
        if (e.key === 'Enter') {
            this.login();
        }
    };

    login = () => {
        this.props.login(this.state.username, this.state.password);
        this.setState({mask: true, showAlert: true});
    };

    onForgotPassword = () => {
        transitionTo(Routes.passwordReset)
    };

    render() {
        const mask = this.props.status === ACTION_STATUS.PENDING ? (<Mask text={this.i18n('login.progress-mask')}/>) : null;
        return(
            <Panel header={<h3>{this.i18n('login.title')}</h3>} bsStyle='info' className="login-panel">
                {mask}
                {this.state.showAlert && this.props.error &&
                    <div>
                        <AlertMessage type={ALERT_TYPES.DANGER}
                              message={this.i18n('login.error')}/>
                        <br/>
                    </div>}
            <Form horizontal>
                <HorizontalInput type='text' name='username' ref={(input) => { this.usernameField = input; }}
                       label={this.i18n('login.username')} value={this.state.username}
                       onChange={this.onChange} labelWidth={3} onKeyPress={this.onKeyPress}
                       inputWidth={9}/>
                <HorizontalInput type='password' name='password' label={this.i18n('login.password')}
                       value={this.state.password}
                       onChange={this.onChange} labelWidth={3} onKeyPress={this.onKeyPress}
                       inputWidth={9}/>
                <div className="login-forgot-password-block">
                    <Button bsStyle='link' className='login-forgot-password' bsSize='small'
                            onClick={this.onForgotPassword}>{this.i18n('login.forgot-password')}</Button>
                </div>
                <div className="login-buttons">
                    <Button bsStyle='success' bsSize='large' onClick={this.login}
                            disabled={this.props.status === ACTION_STATUS.PENDING}>{this.i18n('login.submit')}</Button>
                    {/*TODO i18n forgot password, click*/}
                </div>
            </Form>
        </Panel>
        )
    }
}

export default connect(mapStateToProps, mapDispatchToProps)(injectIntl(I18nWrapper(Login)));

function mapStateToProps(state) {
    return {
        status: state.auth.status,
        error: state.auth.error
    };
}

function mapDispatchToProps(dispatch) {
    return {
        login: bindActionCreators(login, dispatch),
    }
}