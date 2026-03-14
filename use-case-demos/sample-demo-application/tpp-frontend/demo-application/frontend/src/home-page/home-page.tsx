import type { UserInfo } from "../utility/custom-interfaces.ts";
import HeroSection from "../components/hero/hero-section.tsx";
import MainOuterLayout from "../layouts/main-outer-layout.tsx";

interface HomePageProps {
    appName: string;
    userInfo: UserInfo;
}

const HomePage = ({ appName, userInfo }: HomePageProps) => {
    return (
        <MainOuterLayout appName={appName}>
            <HeroSection userInfo={userInfo} />
        </MainOuterLayout>
    );
};

export default HomePage;
