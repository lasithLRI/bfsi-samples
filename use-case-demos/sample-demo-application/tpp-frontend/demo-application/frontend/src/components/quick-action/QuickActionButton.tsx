
import { IconButton } from "@oxygen-ui/react";
import * as React from "react";
import './quick-action.scss'

interface ActionButtonProps {
    icon?: React.ReactNode;
    name?: string;
    onClick?: (path:string) => void;
}

const QuickActionButton = ({icon,name, onClick} : ActionButtonProps)=>{
    if (!onClick) return null;
    const isDisabled = name === "Pay Bills"? false : true;

    return (
        <>
            <div className={!isDisabled ? 'pay-bills-button' : ''}>
                <IconButton className="action-button" disabled={isDisabled}
                            onClick={()=> onClick(
                                `${name?.toLowerCase().replace(' ','')}`)}>
                    {icon}
                    <p>{name}</p>
                </IconButton>
            </div>

        </>
    );
}

export default QuickActionButton;
