#!/bin/bash
pause(){
  read -p "Press [Enter] key to continue..." fackEnterKey
}
one()
{
appfolder=/u01/eirsapp
appfolderdir=/u01/eirsapp/seatelfileprocess
datafolder=/u02/eirsdata/logs/seatelfileprocess

mkdir -p ${appfolderdir}
mkdir -p ${datafolder}

echo `tar -xzvf seatelfileprocess_1.0.0.0.tar.gz >> seatelfileprocess_Module_untar_$(date +%Y%m%d)_log.txt`
echo `mv seatelfileprocess/* ${appfolderdir}`

echo "++++++++++++Module Installation completed+++++++++++"
pause
}

#two()
#{
#	./eirs_sms_ussd_summary_module_db_installation.sh
#pause
#}


show_menus() {
        clear
echo -e ""
echo -e ""
echo -e "         #############################################################"
echo -e "         ##        EIRS Seatel File Process MODULE INSTALLATION     ##"
echo -e "         ##~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~##"
echo -e "         ##                    M A I N - M E N U                    ##"
echo -e "         ##~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~##"
echo -e "         ##                                                         ##"
echo -e "         ##            \033[1m*Please Chose Anyone Action*\033[0m   ##"          #//Don't edit this line
echo -e "         ##                                                         ##"
echo -e "         ##             1.    Install Module                        ##"
echo -e "         ##             2.    Exit                                  ##"
echo -e "         #############################################################"
echo -e ""
             }

read_options(){
        local choice
        read -p "Enter choice [ 1 - 2] " choice
        case $choice in
                1) one ;;
                2) exit 0;;
                *) echo -e "${RED}Error...${STD}" && sleep 2
        esac
}

# ----------------------------------------------
# Step #3: Trap CTRL+C, CTRL+Z and quit singles
# ----------------------------------------------
trap '' SIGINT SIGQUIT SIGTSTP

# -----------------------------------
# Step #4: Main logic - infinite loop
# ------------------------------------
while true
do

        show_menus
        read_options
done
