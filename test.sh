


tests=(~/Documents/workspace/testset2014/stndrd*)

cd ~/Documents/workspace

for test in ${tests[@]}
   do
      java -jar net_simplex.jar $test
   done