#!/bash/bin

## REQUIRES 2 ARGUMENTS.
## ARG 1 : FILE ID
## ARG 2 : DIRECTORY PATH TO OUTPUT

if ([ -z $1 ] || [ -z $2 ]); then
  echo "Argument error need two errors"
  exit -1
fi

FILE=$2/$1


wget https://raw.githubusercontent.com/jolf/NAS-research/master/src/main/java/dk/netarkivet/research/utils/StreamUtils.java -O $FILE


