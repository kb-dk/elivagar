#!/bash/bin

## REQUIRES 2 ARGUMENTS.
## ARG 1 : INPUT FILE
## ARG 2 : PATH TO OUTPUT FILE

if ([ ! -e $1 ] || [ -z $2 ]); then
  echo "Argument error."
  echo "Requires 2 arguments:"
  echo "1. Input file, which must exist"
  echo "2. Output file"
  exit -1
fi


fits.sh -i $1 -xc -o $2
