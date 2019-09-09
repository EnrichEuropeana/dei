#!/usr/bin/env bash

function printUsage() {
	echo Usage:
	echo pdf_to_pyramid_tiff.sh INPUT_PDF_ABSOLUTE_PATH OUTPUT_FOLDER_PATH
	echo Both folders have to exist
}

function process() {
echo pdf_to_pyramid_tiff

# get number of pages
nop=$(pdfinfo "$pdfFile" | grep Pages: | awk '{print $2}')

for (( i=0; i<$nop; i++ ))
do
pageNo=$(printf -v i "%04d" $i ; echo $i)
vips pdfload "$pdfFile" "$pdfFile$pageNo.tif" --page $i
done

for (( i=0; i<$nop; i++ ))
do
pageNo=$(printf -v i "%04d" $i ; echo $i)
name=$(basename -- "$pdfFile")
name="${name%.*}"
vips tiffsave "$pdfFile$pageNo.tif" "$outFolder/${name}_$pageNo.tif" --compression=jpeg --Q=70 --tile --tile-width=512 --tile-height=512 --pyramid
done

for (( i=0; i<$nop; i++ ))
do
pageNo=$(printf -v i "%04d" $i ; echo $i)
rm "$pdfFile$pageNo.tif"
done

echo Done
exit 0
}

if [ $# -lt 2 ]
then
	printUsage
else
	pdfFile=$1
	outFolder=$2
	process
fi
