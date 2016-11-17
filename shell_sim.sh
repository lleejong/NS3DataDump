#!/bin/bash 
#2

#default sime time = 3
SIM_TIME=13

args=("$@")
ARGC=$#


if [ $ARGC -gt 0 ]; then
	if [ $(echo "${args[1]} > 2.0" | bc) -ne 0 ] 
	    then
		SIM_TIME=13; 
	fi
fi


echo SIM_TIME=$SIM_TIME

echo "TX_ERR=${args[0]} TX_DELAY=${args[1]} TX_JITTER=${args[2]}   RX_LOSS=${args[3]} RX_DELAY=${args[4]} RX_JITTER=${args[5]}"

(cd /var/lib/dce-linux-dev/source/ns3-dce;
./waf --run "kupakupa  --TypeOfConnection=p --tcp_cc=cubic --ModeOperation=false \
--udp_bw=155Mbps --SimuTime=$SIM_TIME \
--user_bw_down=155Mbps --user_bw_up=155Mbps \
--tcp_mem_user=409600,8388608,8388608 \
--tcp_mem_server=409600,8388608,8388608 \
--tcp_mem_user_wmem=1048576,3145728,4194304 \
--tcp_mem_user_rmem=2097152,4194304,8388608 \
--tcp_mem_server_wmem=1048576,3145728,4194304 \
--tcp_mem_server_rmem=2097152,4194304,8388608 \
--ErrorModel=1 --errRate=${args[0]} --ErrorModel2=1 --errRate2=${args[3]} \
--chan_k_dw=1.500 --avg_delay_dw=${args[4]} --delay_pdv_dw=${args[5]} --chan_k_up=1.500 --avg_delay_up=${args[1]} --delay_pdv_up=${args[2]}";
cat files-0/var/log/*/stdout | \
awk 'BEGIN { thr = 0;} {if(index($3, "0.0") > 0) { if( index($7, "MBytes") > 0 ) {thr = $8;} else {thr = $7;} }} END { printf("%.2f Mbps \n", thr); }';
)
