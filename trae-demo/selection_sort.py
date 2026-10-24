def selection_sort(arr):
    n = len(arr)
    for i in range(n - 1):
        min_index = i
        for j in range(i + 1, n):
            if arr[j] < arr[min_index]:
                min_index = j
        if min_index != i:
            arr[i], arr[min_index] = arr[min_index], arr[i]
    return arr


if __name__ == "__main__":
    arr = [38, 27, 43, 3, 9, 82, 10]
    print("排序前:", arr)
    selection_sort(arr)
    print("排序后:", arr)
