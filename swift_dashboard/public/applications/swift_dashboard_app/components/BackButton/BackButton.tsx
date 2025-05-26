import React from 'react';
import './BackButton.scss';

interface BackButtonProps {
    data: string;
    onClick: () => void;
}

const BackButton: React.FC<BackButtonProps> = ( { data, onClick }) => {
    return (
        <button className='backButton' onClick={onClick}>
            {data}
        </button>
    );
};

export default BackButton;