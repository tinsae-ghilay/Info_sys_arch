

# Top-Level Funktion Ihres Programms
def length(mylist):
    # DEINE ANTWORT HIER
    return 0 if(len(mylist)==0) else  1 + length(mylist[1:])

# Diese Zelle können Sie zum Testen Ihres Programms verwenden

print(length([1, 2, 3, 4, 5, 6]))

# Top-Level Funktion Ihres Programms
def linear_search(mylist, value):
    # DEINE ANTWORT HIER
    # look for value recursively inside this function
    def find(list):
        return 0 if len(list)== 0 or list[0]== value else 1 + find(list[1:])
    # return what we get recursively
    x = find(mylist)
    # return return value of find() if it is less than list length, else -1(not found)
    return x if x < len(mylist) else -1


# Diese Zelle können Sie zum Testen Ihres Programms verwenden

print(linear_search([1, 2, 3, 4, 5], 13))


# Top-Level Funktion Ihres Programms
def remove_from_list(mylist, value):
    # DEINE ANTWORT HIER
    return [num for num in mylist if num != value] if len(mylist) != 0 else "Provided list is empty"

# Diese Zelle können Sie zum Testen Ihres Programms verwenden

print(remove_from_list([1, 1, 2, 2, 3, 3, 4, 4, 5, 5], 5))
