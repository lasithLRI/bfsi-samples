import { IconButton } from "@oxygen-ui/react";
import type { FC } from "react";
// @ts-ignore
import { ArrowRightFromBracketIcon } from '@oxygen-ui/react-icons';
import './header.scss';

export interface HeaderProps {
    name: string;
}

const Header: FC<HeaderProps> = ({ name }) => {
    return (
        <div className="header-outer">
            <p>{name}</p>
            <IconButton style={{ color: 'white' }}>
                <ArrowRightFromBracketIcon size={24} />
            </IconButton>
        </div>
    );
}

export default Header;
