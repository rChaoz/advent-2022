@echo off
cd data
mkdir day%1
cd day%1
touch input.txt
touch test_in.txt
touch test_ref.txt

cd ..\..

echo fun day%1(data: PuzzleData) = puzzle(data) { input -^>>> "src\main\kotlin\day%1.kt"
echo }>> "src\main\kotlin\day%1.kt"