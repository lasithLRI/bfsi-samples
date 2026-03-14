
import "./hero-section.scss"
// @ts-ignore
import {ArrowLeftArrowRightIcon, UserGroupIcon, ClockAsteriskIcon, BoltIcon} from '@oxygen-ui/react-icons';
import {Box, Grid} from "@oxygen-ui/react";
import {useMediaQuery, useTheme} from "@mui/material";
//import { useNavigate} from "react-router-dom";
//import type {AppInfo, User} from "../../../hooks/config-interfaces.ts";
import QuickActionButton from "../quick-action/QuickActionButton.tsx";
import type {UserInfo} from "../../utility/custom-interfaces.ts";

interface ActionButton {
    icon: React.ReactNode;
    name: string;
}


interface HeroSectionProps {
    userInfo: UserInfo;
    // appInfo: AppInfo;

}


const HeroSection = ({userInfo}:HeroSectionProps) => {
    const isLargeScreen = useMediaQuery(useTheme().breakpoints.down('md'));
    // const navigate = useNavigate();
    const responsiveDirections = isLargeScreen ? 'column' : 'row';
    const responsiveMinHeight = isLargeScreen ? '4rem' : '8rem';
    const responsiveDisplay = isLargeScreen ? 'none' : 'flex';
    const responsivePadding = isLargeScreen ? '1rem' : '4rem';
    const actionButtons: ActionButton[] = [
        {icon: <BoltIcon size={'medium'}/>, name: "Pay Bills"},
        {icon: <ArrowLeftArrowRightIcon size={'medium'}/>, name: "Transfer"},
        {icon: <ClockAsteriskIcon size={'medium'}/>, name: "Schedule"},
        {icon: <UserGroupIcon size={'medium'}/>, name: "Payees"},
    ];
    const onClickHandlerActionButtons = (pathTo:string)=>{
        // const absolutePath = "/"+appInfo.route+"/"+pathTo;
        // navigate(absolutePath);
        console.log("Quick action button clicked", pathTo);
    }
    const greetingSelection = () => {
        const currentHour = new Date().getHours();
        if (currentHour >= 5 && currentHour < 12) {
            return ", Good Morning!";
        } else if (currentHour >= 0 && currentHour < 5) {
            return ", Good Night!";
        } else if (currentHour >= 12 && currentHour < 18) {
            return  ", Good Afternoon!";
        } else {
            return  ", Good Evening!";
        }
    }

    return (
        <>
            <Grid container className='hero-outer' direction={responsiveDirections}
                  sx={{padding: responsivePadding, backgroundImage: `url(${userInfo.background})`}}>
                <Grid className='hero-inner-secton user-info'>
                    <Box className='avatar-container' sx={{display: responsiveDisplay}}>
                        <img src={userInfo.image} alt='avatar' className='avatar' />
                    </Box>
                    <p className="introduction">Hello,<br/><span>{userInfo.name}{greetingSelection()}</span></p>
                </Grid>
                <Grid className='hero-inner-secton actions' sx={{minHeight: responsiveMinHeight}}>
                    {actionButtons.map((button, index) => {
                        return (
                            <QuickActionButton icon={button.icon} name={button.name} key={index}
                                               onClick={onClickHandlerActionButtons}/>
                        );
                    })}
                </Grid>
            </Grid>
        </>
    );
}

export default HeroSection;
