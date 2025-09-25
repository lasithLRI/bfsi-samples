import {useEffect, useState} from "react";


const useUserInfo = ()=>{

    const [userInfo, setUserInfo] = useState(null);

    useEffect(() => {
        const fetchData = async () => {
            const response = await fetch("/configurations/config.json");
            const data = await response.json();
            setUserInfo(data.user);
        }
        fetchData();
    })
    return userInfo;
}

export default useUserInfo;