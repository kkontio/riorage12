#!/bin/bash
DEFAULT_INPUT="./datasets/life_800_10000.txt"
DEFAULT_OUTPUT="./tmp/life_results.txt"
DEFAULT_THREADS="0"
input=$DEFAULT_INPUT
output=$DEFAULT_OUTPUT
threads=$DEFAULT_THREADS

# First parameter given is the location of the input file, the second
# is the location and filename of the desired output file, third one
# is the number of threads to be used.
# If no parameters are provided, the defaults are used.

print_help() {
        echo "Usage: `basename $0` -i input -o output -t num_of_threads"
}

while getopts ":ho:i:t:" opt; do
        case $opt in

                h)
                print_help
                exit 1
                ;;

                o)
                output=$OPTARG
                ;;

                i)
                input=$OPTARG
                ;;

                t)
                threads=$OPTARG
                ;;
        esac
done

java -jar dist/riorage12.jar $input $output $threads

