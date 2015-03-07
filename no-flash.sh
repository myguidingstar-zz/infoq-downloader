cd "$1/slides"

for img in *.swf
do
    echo Converting slide "$img"
    swfrender "$img" -o "${img%.swf}.png"
    rm "$img"
done

cd "$1"
for video in *.flv
do
    echo Converting video "$video"
    avconv -threads auto -i "$video" "${video%.flv}.webm"
done
