/**
 * Copyright (c) 2019-2025, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import "./ActionArea.css"
import {QuickActionButton} from "../../components/AppCommonComponents.jsx";
import {useContext} from "react";
import ConfigContext from "../../../context/ConfigContext.jsx";
import PayBillsIcon from "/public/resources/assets/images/icons/pay_icon.svg?react"
import TransferFundsIcon from "/public/resources/assets/images/icons/transfer_icon.svg?react"
import ScheduleIcon from "/public/resources/assets/images/icons/schedule_icon.svg?react"
import ManagePayeeIcon from "/public/resources/assets/images/icons/payees_icon.svg?react"




const onclickAction = ()=>{
    alert("Quick action Button clicked")
}

const quickActions = [
    { icon: PayBillsIcon, name: "Pay Bills", onClick: onclickAction },
    { icon: TransferFundsIcon, name: "Transfer", onClick: onclickAction },
    { icon: ScheduleIcon, name: "Schedule", onClick: onclickAction },
    { icon: ManagePayeeIcon, name: "Payees", onClick: onclickAction }
];

const ActionArea = ()=>{

    const{userinfo} = useContext(ConfigContext);

    return (
        <>
            <div className="actions-outer">
                <div className="profile-section">
                    <div className="profile-image" style={{backgroundImage:`url(${userinfo.image})`}}></div>
                    <div className="greeting-and-name">
                        <p>Hello,</p>
                        <p className="greeting-second-para">{userinfo.name}, Good Evening !</p>
                    </div>
                </div>
                <div className="quick-action-area">

                    {quickActions.map((action,index)=>(
                        <QuickActionButton key={index} icon={< action.icon/>} name={action.name} onClick={action.onClick}/>
                    ))}

                </div>
            </div>
        </>
    )
}

export default ActionArea;

